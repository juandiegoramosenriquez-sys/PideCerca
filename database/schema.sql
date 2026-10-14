CREATE TABLE IF NOT EXISTS usuario (
    id              BIGSERIAL PRIMARY KEY,
    nombre          VARCHAR(255),
    apellido        VARCHAR(255),
    correo          VARCHAR(255) NOT NULL UNIQUE,
    telefono        VARCHAR(255),
    password        VARCHAR(255),
    rol             VARCHAR(255),
    activo          BOOLEAN,
    fecha_registro  TIMESTAMP(6)
);

CREATE TABLE IF NOT EXISTS negocio (
    id               BIGSERIAL PRIMARY KEY,
    nombre           VARCHAR(255),
    descripcion      VARCHAR(255),
    tipo_comida      VARCHAR(255),
    direccion        VARCHAR(255),
    telefono         VARCHAR(255),
    imagen           VARCHAR(255),
    horario          VARCHAR(255),
    latitud          DOUBLE PRECISION,
    longitud         DOUBLE PRECISION,
    tiempo_estimado  INTEGER,
    abierto          BOOLEAN,
    activo           BOOLEAN,
    usuario_id       BIGINT REFERENCES usuario (id)
);

CREATE TABLE IF NOT EXISTS categoria (
    id           BIGSERIAL PRIMARY KEY,
    nombre       VARCHAR(255),
    descripcion  VARCHAR(255),
    activo       BOOLEAN,
    negocio_id   BIGINT REFERENCES negocio (id)
);

CREATE TABLE IF NOT EXISTS producto (
    id            BIGSERIAL PRIMARY KEY,
    nombre        VARCHAR(255),
    descripcion   VARCHAR(255),
    precio        DOUBLE PRECISION,
    imagen        VARCHAR(255),
    disponible    BOOLEAN,
    negocio_id    BIGINT REFERENCES negocio (id),
    categoria_id  BIGINT REFERENCES categoria (id)
);

CREATE TABLE IF NOT EXISTS pedido (
    id                  BIGSERIAL PRIMARY KEY,
    fecha               TIMESTAMP(6),
    total               DOUBLE PRECISION,
    estado              VARCHAR(255) CHECK (estado IN ('PENDIENTE', 'ACEPTADO', 'EN_PREPARACION', 'LISTO_PARA_RECOGER', 'RECOGIDO', 'CANCELADO')),
    tiempo_preparacion  INTEGER,
    fecha_aceptado      TIMESTAMP(6),
    fecha_listo         TIMESTAMP(6),
    fecha_recogido      TIMESTAMP(6),
    notas               VARCHAR(255),
    motivo_cancelacion  VARCHAR(255),
    usuario_id          BIGINT REFERENCES usuario (id),
    negocio_id          BIGINT REFERENCES negocio (id)
);

CREATE TABLE IF NOT EXISTS detalle_pedido (
    id               BIGSERIAL PRIMARY KEY,
    cantidad         INTEGER,
    precio_unitario  DOUBLE PRECISION,
    subtotal         DOUBLE PRECISION,
    pedido_id        BIGINT REFERENCES pedido (id),
    producto_id      BIGINT REFERENCES producto (id)
);

CREATE TABLE IF NOT EXISTS pago (
    id         BIGSERIAL PRIMARY KEY,
    monto      DOUBLE PRECISION,
    metodo     VARCHAR(255),
    fecha      TIMESTAMP(6),
    estado     VARCHAR(255) CHECK (estado IN ('PENDIENTE', 'PAGADO', 'REEMBOLSO_PENDIENTE', 'REEMBOLSADO', 'FALLIDO')),
    pedido_id  BIGINT UNIQUE REFERENCES pedido (id)
);

CREATE TABLE IF NOT EXISTS notificacion (
    id          BIGSERIAL PRIMARY KEY,
    titulo      VARCHAR(255),
    mensaje     VARCHAR(255),
    leida       BOOLEAN,
    fecha       TIMESTAMP(6),
    usuario_id  BIGINT REFERENCES usuario (id),
    pedido_id   BIGINT REFERENCES pedido (id)
);

CREATE INDEX IF NOT EXISTS idx_pedido_negocio_estado ON pedido (negocio_id, estado);
CREATE INDEX IF NOT EXISTS idx_pedido_usuario ON pedido (usuario_id);
CREATE INDEX IF NOT EXISTS idx_producto_negocio ON producto (negocio_id);
CREATE INDEX IF NOT EXISTS idx_notificacion_usuario_leida ON notificacion (usuario_id, leida);
