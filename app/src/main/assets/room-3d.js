'use strict';

// Modelo ilustrativo de una habitación. Recibe en el hash de la URL el JSON que guarda la API
// (ver unihogar-api/src/modelo3d.js) y acomoda los muebles junto a las paredes sin superponerlos.

const WALL_HEIGHT = 2.5;
const WALL = 0.1;
const WALL_COLORS = {CREMA: '#efe2cb', BLANCO: '#f2efe8', CELESTE: '#d6e4e8', VERDE_SUAVE: '#dfe6d2'};
const LINEN_COLORS = {AZUL: '#4f6d93', VERDE: '#7f9160', GRIS: '#8d9097', TERRACOTA: '#a9684f'};
const FLOORS = {
  MADERA: {tiles: ['#cfa67c', '#c79f77', '#d4ae84', '#cba37b'], grout: '#9c7653', size: 0},
  BALDOSA_CLARA: {tiles: ['#dcc6a7', '#d6bf9f', '#dac3a3'], grout: '#b8a181', size: 0.45},
  BALDOSA_TERRACOTA: {tiles: ['#c3754d', '#bb6d47', '#c67b52'], grout: '#9a5a3c', size: 0.4}
};
const FURNITURE_NAMES = {
  PUERTA: 'puerta', CAMA_SIMPLE: 'cama simple', CAMA_DOBLE: 'cama doble', VELADOR: 'velador',
  ARMARIO: 'armario', ESCRITORIO: 'escritorio', REPISA: 'repisa', COCINETA: 'cocineta',
  REFRIGERADORA: 'refrigeradora', COMEDOR: 'comedor', ALFOMBRA: 'alfombra', PLANTA: 'planta'
};
const WOOD = '#8f5d38';
const WOOD_LIGHT = '#a8724a';
const WOOD_DARK = '#6c4630';
const BRASS = '#c9b27a';

const canvas = document.querySelector('canvas');
const controls = document.querySelector('footer');

function fail() {
  document.getElementById('error').hidden = false;
  controls.hidden = true;
}

function readModel() {
  const data = JSON.parse(decodeURIComponent(location.hash.slice(1)));
  const size = value => {
    if (typeof value !== 'number' || !Number.isFinite(value)) throw new Error('Medida inválida');
    return Math.min(8, Math.max(2, value));
  };
  const muebles = Array.isArray(data.muebles) ? data.muebles.filter(item => item in FURNITURE_NAMES) : [];
  return {
    width: size(data.ancho),
    depth: size(data.largo),
    floor: FLOORS[data.piso] ?? FLOORS.MADERA,
    wall: WALL_COLORS[data.pared] ?? WALL_COLORS.CREMA,
    linen: LINEN_COLORS[data.ropa] ?? LINEN_COLORS.AZUL,
    window: ['IZQUIERDA', 'CENTRO', 'DERECHA', 'NINGUNA'].includes(data.ventana) ? data.ventana : 'CENTRO',
    furniture: new Set(muebles)
  };
}

// ---------- Matrices ----------

function normalize(v) {
  const length = Math.hypot(...v);
  return v.map(component => component / length);
}

function cross(a, b) {
  return [a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]];
}

const dot = (a, b) => a.reduce((sum, value, i) => sum + value * b[i], 0);
const subtract = (a, b) => a.map((value, i) => value - b[i]);
const clamp = (value, min, max) => Math.max(min, Math.min(max, value));

function viewMatrix(eye, target) {
  const back = normalize(subtract(eye, target));
  const right = normalize(cross([0, 1, 0], back));
  const up = cross(back, right);
  return [
    right[0], up[0], back[0], 0,
    right[1], up[1], back[1], 0,
    right[2], up[2], back[2], 0,
    -dot(right, eye), -dot(up, eye), -dot(back, eye), 1
  ];
}

function projectionMatrix(aspect, fov) {
  const scale = 1 / Math.tan(fov / 2);
  const near = 0.05;
  const far = 60;
  return [
    scale / aspect, 0, 0, 0,
    0, scale, 0, 0,
    0, 0, (far + near) / (near - far), -1,
    0, 0, 2 * far * near / (near - far), 0
  ];
}

// ---------- Geometría ----------

// Cada cara recibe una luz fija: arriba es la más clara y abajo la más oscura.
const FACES = [
  [[0, 1, 2, 3], 0.9], [[5, 4, 7, 6], 0.95],
  [[4, 0, 3, 7], 0.8], [[1, 5, 6, 2], 0.88],
  [[3, 2, 6, 7], 1.04], [[4, 5, 1, 0], 0.92]
];

function rgb(hex) {
  return [1, 3, 5].map(start => Number.parseInt(hex.slice(start, start + 2), 16) / 255);
}

function shade(hex, factor) {
  const color = rgb(hex).map(value => Math.round(clamp(value * factor, 0, 1) * 255));
  return '#' + color.map(value => value.toString(16).padStart(2, '0')).join('');
}

function mesh() {
  const vertices = [];
  return {
    vertices,
    box(x, y, z, w, h, d, hex) {
      if (w <= 0 || h <= 0 || d <= 0) return;
      const color = rgb(hex);
      const left = x - w / 2, right = x + w / 2, top = y + h, front = z + d / 2, back = z - d / 2;
      const corners = [
        [left, y, front], [right, y, front], [right, top, front], [left, top, front],
        [left, y, back], [right, y, back], [right, top, back], [left, top, back]
      ];
      for (const [[a, b, c, e], light] of FACES) {
        const lit = color.map(value => Math.min(1, value * light));
        for (const index of [a, b, c, a, c, e]) vertices.push(...corners[index], ...lit);
      }
    }
  };
}

// Paredes: el mueble mira hacia "inward" y se desplaza a lo largo de "along".
function wallFrames(width, depth) {
  return {
    back: {name: 'back', start: [-width / 2, -depth / 2], along: [1, 0], inward: [0, 1], length: width},
    left: {name: 'left', start: [-width / 2, depth / 2], along: [0, -1], inward: [1, 0], length: depth},
    right: {name: 'right', start: [width / 2, -depth / 2], along: [0, 1], inward: [-1, 0], length: depth},
    front: {name: 'front', start: [width / 2, depth / 2], along: [-1, 0], inward: [0, -1], length: width}
  };
}

function toWorld(frame, t, lx, lz) {
  const a = t + lx;
  return [
    frame.start[0] + frame.along[0] * a + frame.inward[0] * lz,
    frame.start[1] + frame.along[1] * a + frame.inward[1] * lz
  ];
}

function area(frame, t, width, near, far) {
  const [x0, z0] = toWorld(frame, t, -width / 2, near);
  const [x1, z1] = toWorld(frame, t, width / 2, far);
  return {x0: Math.min(x0, x1), x1: Math.max(x0, x1), z0: Math.min(z0, z1), z1: Math.max(z0, z1)};
}

const overlaps = (a, b) => a.x0 < b.x1 - 1e-6 && b.x0 < a.x1 - 1e-6 && a.z0 < b.z1 - 1e-6 && b.z0 < a.z1 - 1e-6;

// Dibuja en coordenadas del mueble: lx a lo largo de la pared, lz desde la pared hacia el cuarto.
function placer(target, frame, t) {
  const alongX = frame.along[0] !== 0;
  return (lx, y, lz, lw, h, ld, hex) => {
    const [x, z] = toWorld(frame, t, lx, lz + ld / 2);
    target.box(x, y, z, alongX ? lw : ld, h, alongX ? ld : lw, hex);
  };
}

const BUILDERS = {
  PUERTA(put) {
    put(0, 0, 0, 0.9, 2.05, 0.035, WOOD);
    put(0, 0.22, 0.035, 0.62, 0.72, 0.008, WOOD_DARK);
    put(0, 1.08, 0.035, 0.62, 0.78, 0.008, WOOD_DARK);
    for (const side of [-1, 1]) put(side * 0.48, 0, 0, 0.06, 2.11, 0.05, '#e6d7c0');
    put(0, 2.05, 0, 1.02, 0.06, 0.05, '#e6d7c0');
    put(0.34, 0.98, 0.04, 0.05, 0.05, 0.05, BRASS);
  },
  CAMA(put, {width, linen}) {
    const length = 2;
    for (const dx of [-1, 1]) for (const z of [0.06, length - 0.12]) {
      put(dx * (width / 2 - 0.06), 0, z, 0.07, 0.22, 0.07, WOOD);
    }
    put(0, 0.2, 0.04, width + 0.04, 0.15, length - 0.04, WOOD);
    put(0, 0.35, 0.08, width - 0.04, 0.2, length - 0.12, '#f6efe2');
    put(0, 0.2, 0, width + 0.08, 0.75, 0.07, WOOD);
    put(0, 0.55, 0.62, width + 0.02, 0.06, length - 0.66, linen);
    put(0, 0.3, length - 0.09, width + 0.04, 0.31, 0.04, linen);
    for (const side of [-1, 1]) put(side * (width / 2 + 0.01), 0.3, 0.62, 0.02, 0.31, length - 0.67, linen);
    const stripe = shade(linen, 1.35);
    for (let i = 0; i < 4; i++) put(0, 0.611, 0.8 + i * 0.28, width + 0.025, 0.004, 0.05, stripe);
    put(0, 0.611, 0.66, 0.05, 0.004, length - 0.72, stripe);
    const pillows = width > 1.2 ? [-width / 4, width / 4] : [0];
    for (const x of pillows) put(x, 0.55, 0.13, Math.min(0.62, width * 0.7), 0.12, 0.36, '#fbf6ec');
  },
  VELADOR(put) {
    put(0, 0, 0, 0.45, 0.55, 0.4, WOOD);
    put(0, 0.3, 0.4, 0.37, 0.16, 0.01, WOOD_LIGHT);
    put(0, 0.37, 0.41, 0.06, 0.02, 0.02, BRASS);
    put(0.06, 0.55, 0.12, 0.13, 0.03, 0.13, '#d8c9a8');
    put(0.06, 0.58, 0.17, 0.025, 0.2, 0.025, '#5a4a3c');
    put(0.06, 0.76, 0.08, 0.22, 0.17, 0.2, '#f2dcb0');
  },
  ARMARIO(put) {
    put(0, 0, 0, 0.95, 2, 0.58, WOOD);
    for (const side of [-1, 1]) {
      put(side * 0.235, 0.42, 0.58, 0.44, 1.5, 0.008, WOOD_LIGHT);
      put(side * 0.04, 1.08, 0.588, 0.02, 0.18, 0.02, BRASS);
    }
    put(0, 0.08, 0.58, 0.88, 0.28, 0.008, WOOD_LIGHT);
    put(0, 0.2, 0.588, 0.16, 0.02, 0.02, BRASS);
  },
  ESCRITORIO(put) {
    put(0, 0.72, 0, 1.15, 0.04, 0.58, WOOD);
    put(0.38, 0, 0.02, 0.36, 0.72, 0.54, WOOD);
    for (let i = 0; i < 3; i++) {
      put(0.38, 0.06 + i * 0.22, 0.56, 0.32, 0.19, 0.008, WOOD_LIGHT);
      put(0.38, 0.14 + i * 0.22, 0.568, 0.1, 0.018, 0.018, BRASS);
    }
    for (const z of [0.04, 0.49]) put(-0.53, 0, z, 0.05, 0.72, 0.05, WOOD);
    put(-0.12, 0.76, 0.12, 0.34, 0.02, 0.24, '#3b3f46');
    put(-0.12, 0.78, 0.1, 0.34, 0.22, 0.015, '#4a4f57');
    put(0.33, 0.76, 0.08, 0.22, 0.06, 0.16, '#4f6d93');
    put(0.47, 0.76, 0.26, 0.07, 0.1, 0.07, '#5b7da0');
    const seat = '#2f3238';
    put(-0.1, 0.45, 0.66, 0.46, 0.07, 0.44, seat);
    put(-0.1, 0.52, 1.04, 0.44, 0.5, 0.05, seat);
    for (const dx of [-0.19, 0.19]) for (const z of [0.7, 1.02]) put(-0.1 + dx, 0, z, 0.035, 0.45, 0.035, '#3d3d3d');
  },
  COCINETA(put) {
    put(0, 0, 0, 1.5, 0.86, 0.6, '#c49a68');
    put(0, 0.86, 0, 1.54, 0.04, 0.62, '#e3ddd2');
    for (const side of [-1, 1]) put(side * 0.37, 0.08, 0.6, 0.7, 0.7, 0.008, '#d2aa78');
    put(0, 0.08, 0.605, 0.012, 0.7, 0.006, '#9b7550');
    put(0, 0.9, 0, 1.54, 0.5, 0.015, '#e9e3d8');
    put(-0.35, 0.885, 0.12, 0.5, 0.02, 0.38, '#aeb4ba');
    put(-0.35, 0.9, 0.04, 0.04, 0.25, 0.04, '#c9ccd0');
    put(-0.35, 1.12, 0.04, 0.04, 0.03, 0.16, '#c9ccd0');
    put(0.4, 0.9, 0.1, 0.55, 0.03, 0.42, '#2d2f33');
    for (const dx of [-0.13, 0.13]) for (const z of [0.15, 0.33]) put(0.4 + dx, 0.93, z, 0.12, 0.006, 0.12, '#5a5f66');
  },
  REFRIGERADORA(put) {
    put(0, 0, 0, 0.65, 1.7, 0.65, '#c6cacf');
    put(0, 1.12, 0.65, 0.63, 0.012, 0.006, '#9aa0a6');
    put(-0.25, 1.2, 0.655, 0.03, 0.35, 0.03, '#8a9096');
    put(-0.25, 0.62, 0.655, 0.03, 0.35, 0.03, '#8a9096');
  },
  PLANTA(put) {
    put(0, 0, 0.05, 0.3, 0.3, 0.3, '#a6644b');
    put(0, 0.28, 0.04, 0.33, 0.06, 0.33, '#ba7a58');
    put(0, 0.33, 0.17, 0.05, 0.3, 0.05, '#456344');
    put(-0.08, 0.5, 0.12, 0.2, 0.09, 0.14, '#779262');
    put(0.08, 0.58, 0.16, 0.21, 0.09, 0.14, '#55734c');
    put(0, 0.66, 0.09, 0.15, 0.08, 0.16, '#81966b');
  },
  REPISA(put) {
    put(0, 1.45, 0, 0.9, 0.03, 0.24, WOOD);
    for (const side of [-1, 1]) put(side * 0.35, 1.33, 0, 0.03, 0.12, 0.2, '#3a3a3a');
    const books = ['#7a3b3b', '#4f6d93', '#d8b45c', '#55734c', '#8c5a7a', '#c9c2b4'];
    books.forEach((color, i) => put(-0.36 + i * 0.05, 1.48, 0.04, 0.045, 0.21 - (i % 3) * 0.03, 0.17, color));
    put(0.26, 1.48, 0.06, 0.13, 0.12, 0.13, '#b06a4c');
    put(0.26, 1.6, 0.05, 0.2, 0.1, 0.18, '#5e7d4f');
    put(0.31, 1.24, 0.16, 0.07, 0.25, 0.04, '#6d8f58');
  }
};

const ITEMS = {
  PUERTA: {width: 0.95, depth: 0.95, height: 2.1, mounted: true, walls: [['left', 1], ['front', -1], ['right', -1]]},
  CAMA_SIMPLE: {width: 1, depth: 2, height: 0.95, clearance: 0.45, build: 'CAMA',
    walls: [['back', 1], ['back', -1], ['left', -1], ['right', 1]]},
  CAMA_DOBLE: {width: 1.45, depth: 2, height: 0.95, clearance: 0.45, build: 'CAMA',
    walls: [['back', 1], ['back', -1], ['left', -1], ['right', 1]]},
  VELADOR: {width: 0.45, depth: 0.4, height: 0.94, clearance: 0.3, besides: ['CAMA_SIMPLE', 'CAMA_DOBLE'],
    walls: [['back', 1], ['left', -1], ['right', 1]]},
  COCINETA: {width: 1.54, depth: 0.62, height: 1.4, clearance: 0.75,
    walls: [['back', -1], ['right', 1], ['back', 1], ['left', -1]]},
  REFRIGERADORA: {width: 0.65, depth: 0.66, height: 1.7, clearance: 0.6, besides: ['COCINETA'],
    walls: [['back', -1], ['left', -1], ['right', 1], ['front', -1]]},
  // Los muebles altos prefieren el fondo y la izquierda para no tapar la vista general.
  ARMARIO: {width: 0.95, depth: 0.6, height: 2, clearance: 0.6,
    walls: [['left', -1], ['back', 1], ['left', 1], ['back', -1], ['front', -1], ['right', 1]]},
  ESCRITORIO: {width: 1.15, depth: 0.58, height: 0.8, clearance: 0.75,
    walls: [['right', 1], ['back', -1], ['front', -1], ['left', 1], ['back', 1]]},
  PLANTA: {width: 0.4, depth: 0.4, height: 0.8, clearance: 0,
    walls: [['front', 1], ['right', -1], ['back', -1], ['left', 1], ['front', -1]]}
};
const ORDER = ['PUERTA', 'ARMARIO', 'COCINETA', 'REFRIGERADORA', 'CAMA_SIMPLE', 'CAMA_DOBLE', 'VELADOR', 'ESCRITORIO',
  'COMEDOR', 'PLANTA', 'REPISA', 'ALFOMBRA'];

function makeRoom(room) {
  const {width, depth} = room;
  const base = mesh();
  const outsideOnly = mesh();
  const insideOnly = mesh();
  const frames = wallFrames(width, depth);
  const bounds = {x0: -width / 2, x1: width / 2, z0: -depth / 2, z1: depth / 2};
  const solids = [];
  const clearances = [];
  const placed = {};
  const missing = [];

  // Ventana en la pared del fondo.
  const windowWidth = Math.min(1.5, width * 0.4);
  const windowOffset = Math.max(0.35 + windowWidth / 2, width * 0.28);
  const windowX = {IZQUIERDA: -width / 2 + windowOffset, CENTRO: 0, DERECHA: width / 2 - windowOffset}[room.window];
  const sill = 0.95;
  const lintel = 2.15;
  const windowArea = windowX === undefined ? null
    : {from: windowX - windowWidth / 2 - 0.05, to: windowX + windowWidth / 2 + 0.05};

  function fits(frame, t, spec) {
    const body = area(frame, t, spec.width, 0, spec.depth);
    const reach = area(frame, t, spec.width, 0, spec.depth + (spec.clearance ?? 0));
    if (body.x0 < bounds.x0 - 1e-6 || body.x1 > bounds.x1 + 1e-6 || body.z0 < bounds.z0 - 1e-6 || body.z1 > bounds.z1 + 1e-6) {
      return null;
    }
    if (reach.x0 < bounds.x0 - 1e-6 || reach.x1 > bounds.x1 + 1e-6 || reach.z0 < bounds.z0 - 1e-6 || reach.z1 > bounds.z1 + 1e-6) {
      return null;
    }
    if (solids.some(other => overlaps(body, other) || overlaps(reach, other))) return null;
    if (clearances.some(other => overlaps(body, other))) return null;
    if (frame.name === 'back' && windowArea && spec.height > sill) {
      const x = frame.start[0] + t;
      if (x + spec.width / 2 > windowArea.from && x - spec.width / 2 < windowArea.to) return null;
    }
    return {body, reach};
  }

  function positions(frame, spec, direction) {
    const first = spec.width / 2 + 0.02;
    const last = frame.length - spec.width / 2 - 0.02;
    const steps = [];
    for (let t = first; t <= last + 1e-6; t += 0.05) steps.push(t);
    return direction > 0 ? steps : steps.reverse();
  }

  function settle(id, spec, frame, t, area) {
    solids.push(area.body);
    if (spec.clearance) clearances.push(area.reach);
    placed[id] = {frame, t, spec};
  }

  function placeAgainstWall(id) {
    const spec = ITEMS[id];
    const anchor = (spec.besides ?? []).map(other => placed[other]).find(Boolean);
    if (anchor) {
      for (const side of [1, -1]) {
        const t = anchor.t + side * (anchor.spec.width / 2 + spec.width / 2 + 0.04);
        const result = fits(anchor.frame, t, spec);
        if (result) return settle(id, spec, anchor.frame, t, result);
      }
    }
    for (const [wall, direction] of spec.walls) {
      const frame = frames[wall];
      for (const t of positions(frame, spec, direction)) {
        const result = fits(frame, t, spec);
        if (result) return settle(id, spec, frame, t, result);
      }
    }
    missing.push(FURNITURE_NAMES[id]);
  }

  // Mesa con dos sillas, lejos de las paredes y preferiblemente adelante a la izquierda.
  function placeTable() {
    const footprint = {w: 1.55, d: 0.8};
    const candidates = [];
    for (let x = bounds.x0 + footprint.w / 2 + 0.1; x <= bounds.x1 - footprint.w / 2 - 0.1 + 1e-6; x += 0.1) {
      for (let z = bounds.z0 + footprint.d / 2 + 0.1; z <= bounds.z1 - footprint.d / 2 - 0.1 + 1e-6; z += 0.1) {
        candidates.push([x, z, Math.hypot(x + width / 4, z - depth / 4)]);
      }
    }
    candidates.sort((a, b) => a[2] - b[2]);
    for (const [x, z] of candidates) {
      const body = {x0: x - footprint.w / 2, x1: x + footprint.w / 2, z0: z - footprint.d / 2, z1: z + footprint.d / 2};
      if (solids.some(other => overlaps(body, other)) || clearances.some(other => overlaps(body, other))) continue;
      solids.push(body);
      placed.COMEDOR = {x, z, body};
      return;
    }
    missing.push(FURNITURE_NAMES.COMEDOR);
  }

  // La repisa va sobre el escritorio o la cama. Si no hay, busca un tramo libre de pared.
  function placeShelf() {
    const spec = {width: 0.9, depth: 0.25, height: 1.75};
    const tall = Object.values(placed).filter(item => item.spec && item.spec.height > 1.3);
    const free = (frame, t) => {
      const body = area(frame, t, spec.width, 0, spec.depth);
      if (body.x0 < bounds.x0 - 1e-6 || body.x1 > bounds.x1 + 1e-6 || body.z0 < bounds.z0 - 1e-6 || body.z1 > bounds.z1 + 1e-6) {
        return false;
      }
      if (tall.some(item => overlaps(body, area(item.frame, item.t, item.spec.width, 0, item.spec.depth)))) return false;
      if (frame.name === 'back' && windowArea) {
        const x = frame.start[0] + t;
        if (x + spec.width / 2 > windowArea.from && x - spec.width / 2 < windowArea.to) return false;
      }
      return true;
    };
    for (const host of [placed.ESCRITORIO, placed.CAMA_SIMPLE, placed.CAMA_DOBLE, placed.COCINETA]) {
      if (host && free(host.frame, host.t)) {
        placed.REPISA = {frame: host.frame, t: host.t, spec};
        return;
      }
    }
    for (const wall of ['right', 'back', 'left', 'front']) {
      for (const t of positions(frames[wall], spec, 1)) {
        if (free(frames[wall], t)) {
          placed.REPISA = {frame: frames[wall], t, spec};
          return;
        }
      }
    }
    missing.push(FURNITURE_NAMES.REPISA);
  }

  for (const id of ORDER) {
    if (!room.furniture.has(id)) continue;
    if (id === 'COMEDOR') placeTable();
    else if (id === 'REPISA') placeShelf();
    else if (id !== 'ALFOMBRA') placeAgainstWall(id);
  }

  // Piso y base.
  base.box(0, -0.3, 0, width + WALL * 2, 0.282, depth + WALL * 2, '#7d5c42');
  const floor = room.floor;
  if (floor.size) {
    const columns = Math.max(1, Math.round(width / floor.size));
    const rows = Math.max(1, Math.round(depth / floor.size));
    const tileW = width / columns, tileD = depth / rows;
    base.box(0, -0.02, 0, width, 0.012, depth, floor.grout);
    for (let i = 0; i < columns; i++) for (let j = 0; j < rows; j++) {
      base.box(-width / 2 + (i + 0.5) * tileW, -0.018, -depth / 2 + (j + 0.5) * tileD,
        tileW - 0.012, 0.018, tileD - 0.012, floor.tiles[(i * 7 + j * 3) % floor.tiles.length]);
    }
  } else {
    const boards = Math.max(8, Math.round(width / 0.28));
    const strip = width / boards;
    for (let i = 0; i < boards; i++) {
      base.box(-width / 2 + (i + 0.5) * strip, -0.018, 0, strip - 0.01, 0.018, depth, floor.tiles[i % floor.tiles.length]);
    }
  }
  const floorShadow = shade(floor.tiles[0], 0.72);

  // Paredes. Las del frente y la derecha se cortan en la vista general para ver el interior.
  const wall = room.wall;
  const trim = shade(wall, 0.86);
  const backZ = -depth / 2 - WALL / 2;
  const leftX = -width / 2 - WALL / 2;
  const fullLeft = -width / 2 - WALL;
  const fullRight = width / 2 + WALL;
  if (windowX === undefined) {
    base.box(0, 0, backZ, width + WALL * 2, WALL_HEIGHT, WALL, wall);
  } else {
    const from = windowX - windowWidth / 2, to = windowX + windowWidth / 2;
    base.box((fullLeft + from) / 2, 0, backZ, from - fullLeft, WALL_HEIGHT, WALL, wall);
    base.box((to + fullRight) / 2, 0, backZ, fullRight - to, WALL_HEIGHT, WALL, wall);
    base.box(windowX, 0, backZ, windowWidth, sill, WALL, wall);
    base.box(windowX, lintel, backZ, windowWidth, WALL_HEIGHT - lintel, WALL, wall);
    // Vista de las colinas de Guaranda detrás del vidrio.
    const viewZ = -depth / 2 - WALL - 0.25;
    base.box(windowX, sill - 0.1, viewZ, windowWidth + 0.8, lintel - sill + 0.3, 0.02, '#c4dbe6');
    base.box(windowX - windowWidth * 0.2, sill - 0.1, viewZ + 0.05, windowWidth * 0.9, 0.55, 0.02, '#6f8f5d');
    base.box(windowX + windowWidth * 0.25, sill - 0.1, viewZ + 0.08, windowWidth * 0.8, 0.42, 0.02, '#87a46d');
    for (let i = 0; i < 4; i++) {
      base.box(windowX - windowWidth / 2 + 0.18 + i * windowWidth / 4, sill - 0.1, viewZ + 0.11,
        0.16, 0.22 + (i % 2) * 0.08, 0.02, i % 2 ? '#e9e1d4' : '#d99a76');
    }
    const frameColor = '#f4ede0';
    base.box(windowX, sill - 0.04, -depth / 2 + 0.04, windowWidth + 0.14, 0.05, 0.16, frameColor);
    base.box(windowX, sill, -depth / 2 - 0.03, 0.04, lintel - sill, 0.04, frameColor);
    base.box(windowX, (sill + lintel) / 2, -depth / 2 - 0.03, windowWidth, 0.04, 0.04, frameColor);
    for (const side of [-1, 1]) {
      base.box(windowX + side * (windowWidth / 2 - 0.02), sill, -depth / 2 - 0.03, 0.04, lintel - sill, 0.05, frameColor);
      for (let fold = 0; fold < 3; fold++) {
        base.box(windowX + side * (windowWidth / 2 + 0.06 + fold * 0.06), 0.9, -depth / 2 + 0.07 + (fold % 2) * 0.03,
          0.07, 1.35, 0.05, fold % 2 ? '#c8ad86' : '#dcc39c');
      }
    }
    base.box(windowX, lintel + 0.08, -depth / 2 + 0.06, windowWidth + 0.5, 0.03, 0.03, WOOD_DARK);
  }
  base.box(leftX, 0, 0, WALL, WALL_HEIGHT, depth, wall);
  base.box(-width / 2 + 0.01, 0, 0, 0.02, 0.1, depth, trim);
  base.box(0, 0, -depth / 2 + 0.01, width, 0.1, 0.02, trim);
  for (const target of [outsideOnly, insideOnly]) {
    const height = target === insideOnly ? WALL_HEIGHT : 0.14;
    target.box(width / 2 + WALL / 2, 0, 0, WALL, height, depth + WALL * 2, wall);
    target.box(0, 0, depth / 2 + WALL / 2, width, height, WALL, wall);
  }
  outsideOnly.box(width / 2 + WALL / 2, 0.14, 0, WALL, 0.01, depth + WALL * 2, '#7d5c42');
  outsideOnly.box(0, 0.14, depth / 2 + WALL / 2, width, 0.01, WALL, '#7d5c42');
  insideOnly.box(width / 2 - 0.01, 0, 0, 0.02, 0.1, depth, trim);
  insideOnly.box(0, 0, depth / 2 - 0.01, width, 0.1, 0.02, trim);
  insideOnly.box(0, WALL_HEIGHT, 0, width + WALL * 2, 0.06, depth + WALL * 2, '#fffdf8');
  insideOnly.box(0, WALL_HEIGHT - 0.04, 0, 0.3, 0.04, 0.3, '#fff6dc');

  // Muebles junto a las paredes.
  for (const [id, item] of Object.entries(placed)) {
    if (!item.frame) continue;
    const hidden = item.frame.name === 'right' || item.frame.name === 'front';
    const mounted = id === 'PUERTA' || id === 'REPISA';
    const target = hidden && mounted ? insideOnly : base;
    const put = placer(target, item.frame, item.t);
    const spec = ITEMS[id] ?? item.spec;
    if (!mounted) {
      const shadow = area(item.frame, item.t, spec.width + 0.08, 0, spec.depth + 0.04);
      base.box((shadow.x0 + shadow.x1) / 2, 0, (shadow.z0 + shadow.z1) / 2, shadow.x1 - shadow.x0, 0.003, shadow.z1 - shadow.z0, floorShadow);
    }
    BUILDERS[spec.build ?? id](put, {width: spec.width, linen: room.linen});
  }

  if (placed.COMEDOR) {
    const {x, z} = placed.COMEDOR;
    base.box(x, 0, z, 1.6, 0.003, 0.85, floorShadow);
    base.box(x, 0.72, z, 0.85, 0.04, 0.75, WOOD);
    for (const dx of [-1, 1]) for (const dz of [-1, 1]) base.box(x + dx * 0.37, 0, z + dz * 0.31, 0.05, 0.72, 0.05, WOOD);
    base.box(x, 0.76, z, 0.25, 0.004, 0.75, '#a6503c');
    base.box(x, 0.764, z, 0.1, 0.18, 0.1, '#c08a50');
    base.box(x, 0.94, z, 0.2, 0.1, 0.2, '#d9c6e8');
    for (const side of [-1, 1]) {
      base.box(x + side * 0.58, 0.44, z, 0.42, 0.05, 0.42, WOOD_LIGHT);
      base.box(x + side * 0.77, 0.49, z, 0.04, 0.5, 0.42, WOOD_LIGHT);
      for (const dx of [-0.17, 0.17]) for (const dz of [-0.17, 0.17]) {
        base.box(x + side * 0.58 + dx, 0, z + dz, 0.035, 0.44, 0.035, WOOD_DARK);
      }
    }
  }

  if (room.furniture.has('ALFOMBRA')) {
    const bed = placed.CAMA_SIMPLE ?? placed.CAMA_DOBLE;
    let rug = bed
      ? area(bed.frame, bed.t, bed.spec.width + 0.7, bed.spec.depth * 0.45, bed.spec.depth + 0.55)
      : {x0: -0.7, x1: 0.7, z0: -0.5, z1: 0.5};
    rug = {x0: Math.max(rug.x0, bounds.x0 + 0.05), x1: Math.min(rug.x1, bounds.x1 - 0.05),
      z0: Math.max(rug.z0, bounds.z0 + 0.05), z1: Math.min(rug.z1, bounds.z1 - 0.05)};
    const cx = (rug.x0 + rug.x1) / 2, cz = (rug.z0 + rug.z1) / 2;
    base.box(cx, 0.004, cz, rug.x1 - rug.x0, 0.012, rug.z1 - rug.z0, '#c9a77f');
    base.box(cx, 0.0045, cz, rug.x1 - rug.x0 - 0.14, 0.012, rug.z1 - rug.z0 - 0.14, '#b98f66');
  }

  // Punto de vista interior: un lugar libre junto a la pared del frente, mirando hacia el fondo.
  const spots = [];
  for (const z of [depth / 2 - 0.4, depth / 2 - 0.8, 0]) {
    for (const x of [width / 2 - 0.5, 0, -width / 2 + 0.5, width / 4, -width / 4]) spots.push([x, z]);
  }
  const eye = spots.find(([x, z]) => !solids.some(s => x > s.x0 - 0.4 && x < s.x1 + 0.4 && z > s.z0 - 0.4 && z < s.z1 + 0.4))
    ?? [0, depth / 2 - 0.4];
  const yaw = Math.atan2(eye[0] * 0.6, eye[1] + depth / 4);

  return {
    meshes: [base.vertices, outsideOnly.vertices, insideOnly.vertices],
    missing,
    eye: [eye[0], 1.55, eye[1]],
    yaw
  };
}

function describe(room) {
  const format = value => value.toLocaleString('es-EC', {maximumFractionDigits: 2});
  const list = [...room.furniture].map(id => FURNITURE_NAMES[id]);
  return {
    summary: `Modelo ilustrativo · ${format(room.width)} × ${format(room.depth)} m`,
    label: `Modelo tridimensional de ${format(room.width)} por ${format(room.depth)} metros` +
      (list.length ? ` con ${list.join(', ')}` : '') + '. Arrastra o usa las flechas para mover la cámara.'
  };
}

function start() {
  const room = readModel();
  const scene = makeRoom(room);
  const text = describe(room);
  document.getElementById('summary').textContent = text.summary;
  canvas.setAttribute('aria-label', text.label);
  if (scene.missing.length) {
    const missing = document.getElementById('missing');
    missing.textContent = `No caben con estas medidas: ${scene.missing.join(', ')}`;
    missing.hidden = false;
  }

  const gl = canvas.getContext('webgl', {alpha: false, antialias: true});
  if (!gl) throw new Error('WebGL unavailable');
  const program = gl.createProgram();
  for (const [type, source] of [
    [gl.VERTEX_SHADER, `
      attribute vec3 position;
      attribute vec3 color;
      uniform mat4 view;
      uniform mat4 projection;
      varying vec3 material;
      varying float height;
      void main() {
        material = color;
        height = position.y;
        gl_Position = projection * view * vec4(position, 1.);
      }
    `],
    [gl.FRAGMENT_SHADER, `
      precision mediump float;
      varying vec3 material;
      varying float height;
      void main() {
        // Oscurece un poco la parte baja de paredes y muebles, como la luz que no llega al piso.
        float light = mix(0.88, 1.03, smoothstep(0., 1.6, height));
        gl_FragColor = vec4(min(material * light, vec3(1.)), 1.);
      }
    `]
  ]) {
    const shader = gl.createShader(type);
    gl.shaderSource(shader, source);
    gl.compileShader(shader);
    if (!gl.getShaderParameter(shader, gl.COMPILE_STATUS)) throw new Error(gl.getShaderInfoLog(shader));
    gl.attachShader(program, shader);
    gl.deleteShader(shader);
  }
  gl.linkProgram(program);
  if (!gl.getProgramParameter(program, gl.LINK_STATUS)) throw new Error(gl.getProgramInfoLog(program));
  gl.useProgram(program);
  gl.enable(gl.DEPTH_TEST);

  const counts = scene.meshes.map(part => part.length / 6);
  const all = new Float32Array(scene.meshes.reduce((sum, part) => sum + part.length, 0));
  let offset = 0;
  for (const part of scene.meshes) {
    all.set(part, offset);
    offset += part.length;
  }
  const buffer = gl.createBuffer();
  gl.bindBuffer(gl.ARRAY_BUFFER, buffer);
  gl.bufferData(gl.ARRAY_BUFFER, all, gl.STATIC_DRAW);
  for (const [name, byteOffset] of [['position', 0], ['color', 12]]) {
    const attribute = gl.getAttribLocation(program, name);
    gl.enableVertexAttribArray(attribute);
    gl.vertexAttribPointer(attribute, 3, gl.FLOAT, false, 24, byteOffset);
  }
  const view = gl.getUniformLocation(program, 'view');
  const projection = gl.getUniformLocation(program, 'projection');

  const overview = [0.85, 0.55, 1];
  const inside = [scene.yaw, -0.12, 1];
  const reducedMotion = matchMedia('(prefers-reduced-motion: reduce)').matches;
  let mode = 'orbit';
  let orbit = [...overview];
  let look = [...inside];
  let cameraMix = 0;
  let frame = 0;
  let stopped = false;
  const started = performance.now();
  let intro = !reducedMotion;
  const size = Math.max(room.width, room.depth);

  function render(time) {
    frame = 0;
    const targetMix = mode === 'orbit' ? 0 : 1;
    cameraMix = reducedMotion || Math.abs(targetMix - cameraMix) < 0.012
      ? targetMix : cameraMix + (targetMix - cameraMix) * 0.12;
    const mix = (a, b) => a + (b - a) * cameraMix;
    const [angle, elevation, zoom] = orbit;
    const progress = Math.min(1, (time - started) / 2350);
    const azimuth = angle + (intro ? 0.31 * (1 - progress) ** 3 : 0);
    if (progress === 1) intro = false;
    const distance = Math.max(7.2, size * 1.9) / zoom;
    const outsideEye = [
      distance * Math.sin(azimuth) * Math.cos(elevation),
      0.68 + distance * Math.sin(elevation),
      distance * Math.cos(azimuth) * Math.cos(elevation)
    ];
    const [yaw, pitch, insideZoom] = look;
    const direction = [-Math.sin(yaw) * Math.cos(pitch), Math.sin(pitch), -Math.cos(yaw) * Math.cos(pitch)];
    const insideTarget = scene.eye.map((value, i) => value + direction[i] * 5);
    const eye = outsideEye.map((value, i) => mix(value, scene.eye[i]));
    const target = [0, 0.78, 0].map((value, i) => mix(value, insideTarget[i]));
    const pixelRatio = Math.min(devicePixelRatio, 1.6);
    const width = Math.max(1, Math.round(canvas.clientWidth * pixelRatio));
    const height = Math.max(1, Math.round(canvas.clientHeight * pixelRatio));
    if (canvas.width !== width || canvas.height !== height) {
      canvas.width = width;
      canvas.height = height;
    }
    const aspect = width / height;
    const overviewFov = 2 * Math.atan(0.40 / Math.min(1, aspect));
    const insideFov = 2 * Math.atan(0.8 / Math.min(1, aspect * 1.6)) / insideZoom;
    gl.viewport(0, 0, width, height);
    gl.clearColor(0.918, 0.898, 0.859, 1);
    gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);
    gl.uniformMatrix4fv(view, false, viewMatrix(eye, target));
    gl.uniformMatrix4fv(projection, false, projectionMatrix(aspect, mix(overviewFov, insideFov)));
    gl.drawArrays(gl.TRIANGLES, 0, counts[0]);
    // El techo y las paredes completas aparecen solo con la cámara ya dentro del cuarto.
    if (cameraMix < 0.97) gl.drawArrays(gl.TRIANGLES, counts[0], counts[1]);
    else gl.drawArrays(gl.TRIANGLES, counts[0] + counts[1], counts[2]);
    if (intro || cameraMix !== targetMix) redraw();
  }

  function redraw() {
    if (!stopped && !frame) frame = requestAnimationFrame(render);
  }

  function move(dx, dy) {
    intro = false;
    if (mode === 'orbit') {
      orbit[0] = clamp(orbit[0] - dx, 0.10, 1.50);
      orbit[1] = clamp(orbit[1] + dy, 0.24, 1.15);
    } else {
      look[0] = (look[0] + dx) % (Math.PI * 2);
      look[1] = clamp(look[1] + dy, -0.9, 0.7);
    }
    redraw();
  }

  function setZoom(value) {
    const camera = mode === 'orbit' ? orbit : look;
    camera[2] = clamp(value, 0.78, 1.6);
    redraw();
  }
  const currentZoom = () => (mode === 'orbit' ? orbit : look)[2];

  function select(next) {
    intro = false;
    mode = next;
    for (const id of ['orbit', 'inside']) {
      document.getElementById(id).setAttribute('aria-pressed', String(id === next));
    }
    redraw();
  }

  // Un dedo gira la cámara; dos dedos acercan o alejan.
  const pointers = new Map();
  let pinch = null;
  canvas.addEventListener('pointerdown', event => {
    if (pointers.size >= 2) return;
    pointers.set(event.pointerId, {x: event.clientX, y: event.clientY});
    canvas.setPointerCapture(event.pointerId);
    if (pointers.size === 2) {
      const [a, b] = [...pointers.values()];
      pinch = {distance: Math.hypot(a.x - b.x, a.y - b.y) || 1, zoom: currentZoom()};
    }
  });
  canvas.addEventListener('pointermove', event => {
    const pointer = pointers.get(event.pointerId);
    if (!pointer) return;
    const dx = event.clientX - pointer.x, dy = event.clientY - pointer.y;
    pointer.x = event.clientX;
    pointer.y = event.clientY;
    if (pinch) {
      const [a, b] = [...pointers.values()];
      setZoom(pinch.zoom * Math.hypot(a.x - b.x, a.y - b.y) / pinch.distance);
    } else {
      move(dx * 0.006, dy * 0.006);
    }
  });
  const release = event => {
    pointers.delete(event.pointerId);
    if (pointers.size < 2) pinch = null;
  };
  canvas.addEventListener('lostpointercapture', release);
  canvas.addEventListener('pointercancel', release);
  canvas.addEventListener('wheel', event => {
    event.preventDefault();
    setZoom(currentZoom() + Math.sign(-event.deltaY) * 0.12);
  }, {passive: false});
  canvas.addEventListener('keydown', event => {
    const movement = {ArrowLeft: [-0.14, 0], ArrowRight: [0.14, 0], ArrowUp: [0, -0.14], ArrowDown: [0, 0.14]}[event.key];
    if (movement) {
      event.preventDefault();
      move(...movement);
    } else if (event.key === '+' || event.key === '-') {
      event.preventDefault();
      setZoom(currentZoom() + (event.key === '+' ? 0.15 : -0.15));
    }
  });
  canvas.addEventListener('webglcontextlost', () => {
    stopped = true;
    cancelAnimationFrame(frame);
    fail();
  });
  window.addEventListener('resize', redraw);
  document.getElementById('orbit').onclick = () => select('orbit');
  document.getElementById('inside').onclick = () => select('inside');
  document.getElementById('plus').onclick = () => setZoom(currentZoom() + 0.15);
  document.getElementById('minus').onclick = () => setZoom(currentZoom() - 0.15);
  document.getElementById('reset').onclick = () => {
    orbit = [...overview];
    look = [...inside];
    redraw();
  };
  redraw();
}

try {
  start();
} catch (error) {
  console.error(error);
  fail();
}
