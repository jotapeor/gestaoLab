USE gestaolab_db;

ALTER TABLE reserva_laboratorio
    ADD COLUMN id_cancelado_por BIGINT NULL,
    ADD COLUMN data_cancelamento DATETIME NULL,
    ADD COLUMN motivo_cancelamento VARCHAR(255) NULL,
    ADD CONSTRAINT fk_reserva_lab_cancelado_por
        FOREIGN KEY (id_cancelado_por) REFERENCES usuario (id_usuario) ON DELETE SET NULL,
    ADD INDEX idx_reserva_lab_cancelado_por (id_cancelado_por);
