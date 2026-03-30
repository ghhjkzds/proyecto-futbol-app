-- =============================================================
-- SCHEMA PARA NEON (PostgreSQL)
-- Proyecto Fútbol App
-- Sin tabla comentarios · Sin columna votos
-- =============================================================

-- ─────────────────────────────────────────────
-- TABLA: users
-- ─────────────────────────────────────────────
CREATE TABLE users (
    id          SERIAL PRIMARY KEY,
    email       VARCHAR(150)  NOT NULL UNIQUE,
    password    VARCHAR(255)  NOT NULL,
    role        VARCHAR(20)   NOT NULL DEFAULT 'USER',
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_user_role CHECK (role IN ('USER', 'ADMIN'))
);

-- ─────────────────────────────────────────────
-- TABLA: equipos
-- ─────────────────────────────────────────────
CREATE TABLE equipos (
    id          SERIAL PRIMARY KEY,
    id_user     INTEGER       NOT NULL,
    nombre      VARCHAR(150)  NOT NULL,
    alineacion  JSONB,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_equipo_user
        FOREIGN KEY (id_user) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_equipos_user       ON equipos(id_user);
CREATE INDEX idx_equipos_alineacion ON equipos USING gin(alineacion);

-- ─────────────────────────────────────────────
-- TABLA: partidos
-- ─────────────────────────────────────────────
CREATE TABLE partidos (
    id                   SERIAL PRIMARY KEY,
    equipo_local_id      INTEGER      NOT NULL,
    equipo_visitante_id  INTEGER      NOT NULL,
    fecha                TIMESTAMPTZ  NOT NULL,
    creado_por           INTEGER      NOT NULL,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_partido_local
        FOREIGN KEY (equipo_local_id)     REFERENCES equipos(id) ON DELETE CASCADE,
    CONSTRAINT fk_partido_visitante
        FOREIGN KEY (equipo_visitante_id) REFERENCES equipos(id) ON DELETE CASCADE,
    CONSTRAINT fk_partido_creador
        FOREIGN KEY (creado_por)          REFERENCES users(id)   ON DELETE CASCADE,
    CONSTRAINT chk_equipos_distintos
        CHECK (equipo_local_id <> equipo_visitante_id)
);

CREATE INDEX idx_partidos_local     ON partidos(equipo_local_id);
CREATE INDEX idx_partidos_visitante ON partidos(equipo_visitante_id);
CREATE INDEX idx_partidos_fecha     ON partidos(fecha);
CREATE INDEX idx_partidos_creador   ON partidos(creado_por);

-- ─────────────────────────────────────────────
-- TABLA: alineaciones
-- ─────────────────────────────────────────────
CREATE TABLE alineaciones (
    id          SERIAL PRIMARY KEY,
    partido_id  INTEGER      NOT NULL,
    equipo_id   INTEGER      NOT NULL,
    created_by  INTEGER,
    alineacion  JSONB        NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_alineacion_partido
        FOREIGN KEY (partido_id)  REFERENCES partidos(id) ON DELETE CASCADE,
    CONSTRAINT fk_alineacion_equipo
        FOREIGN KEY (equipo_id)   REFERENCES equipos(id)  ON DELETE CASCADE,
    CONSTRAINT fk_alineacion_user
        FOREIGN KEY (created_by)  REFERENCES users(id)    ON DELETE SET NULL,
    -- Un usuario solo puede crear una alineación por equipo por partido
    CONSTRAINT uk_user_partido_equipo
        UNIQUE (created_by, partido_id, equipo_id)
);

CREATE INDEX idx_alineaciones_partido   ON alineaciones(partido_id);
CREATE INDEX idx_alineaciones_equipo    ON alineaciones(equipo_id);
CREATE INDEX idx_alineaciones_user      ON alineaciones(created_by);
CREATE INDEX idx_alineaciones_json      ON alineaciones USING gin(alineacion);

-- ─────────────────────────────────────────────
-- TABLA: apis
-- ─────────────────────────────────────────────
CREATE TABLE apis (
    id          SERIAL PRIMARY KEY,
    nombre      VARCHAR(100)  NOT NULL,
    id_api      VARCHAR(150)  NOT NULL UNIQUE,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

