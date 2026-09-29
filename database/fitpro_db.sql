-- Esquema de FitPro, alineado con las entidades JPA.
-- No hace falta ejecutarlo si la app arranca con ddl-auto=update: Hibernate crea las tablas.

CREATE DATABASE IF NOT EXISTS fitpro_db;
USE fitpro_db;

CREATE TABLE cliente (
  idCliente INT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(50) NOT NULL,
  apellido VARCHAR(50) NOT NULL,
  documento VARCHAR(30) UNIQUE,
  email VARCHAR(100) UNIQUE,
  telefono VARCHAR(20),
  direccion VARCHAR(100),
  password VARCHAR(255),
  peso DOUBLE,
  altura DOUBLE,
  edad INT,
  genero VARCHAR(20),
  objetivo_fitness VARCHAR(100),
  nivel_actividad VARCHAR(50),
  fecha_registro DATE,
  PRIMARY KEY (idCliente)
);

CREATE TABLE administrador (
  idAdministrador INT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(50),
  apellido VARCHAR(50),
  email VARCHAR(100) UNIQUE,
  telefono VARCHAR(20),
  password VARCHAR(255),
  PRIMARY KEY (idAdministrador)
);

CREATE TABLE entrenador (
  idEntrenador INT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(50),
  especialidad VARCHAR(50),
  horario VARCHAR(50),
  email VARCHAR(100),
  telefono VARCHAR(20),
  PRIMARY KEY (idEntrenador)
);

CREATE TABLE clase (
  idClase INT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(50),
  descripcion TEXT,
  capacidad INT,
  fecha DATE,
  hora TIME(6),
  duracion_minutos INT,
  Entrenador_idEntrenador INT,
  PRIMARY KEY (idClase),
  FOREIGN KEY (Entrenador_idEntrenador) REFERENCES entrenador(idEntrenador)
);

CREATE TABLE cliente_has_clase (
  Cliente_idCliente INT NOT NULL,
  Clase_idClase INT NOT NULL,
  FOREIGN KEY (Cliente_idCliente) REFERENCES cliente(idCliente),
  FOREIGN KEY (Clase_idClase) REFERENCES clase(idClase)
);

CREATE TABLE alimentacion (
  idAlimentacion INT NOT NULL AUTO_INCREMENT,
  idCliente INT,
  tipo_comida VARCHAR(50),
  calorias INT,
  proteinas_gramos DOUBLE,
  carbohidratos_gramos DOUBLE,
  grasas_gramos DOUBLE,
  descripcion TEXT,
  PRIMARY KEY (idAlimentacion),
  FOREIGN KEY (idCliente) REFERENCES cliente(idCliente)
);

CREATE TABLE rutina (
  idRutina INT NOT NULL AUTO_INCREMENT,
  idCliente INT,
  nombre VARCHAR(50),
  objetivo VARCHAR(100),
  nivel VARCHAR(50),
  PRIMARY KEY (idRutina),
  FOREIGN KEY (idCliente) REFERENCES cliente(idCliente)
);

CREATE TABLE ejercicio (
  idEjercicio INT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(100) NOT NULL,
  series INT,
  repeticiones INT,
  descanso_segundos INT,
  notas TEXT,
  Rutina_idRutina INT,
  PRIMARY KEY (idEjercicio),
  FOREIGN KEY (Rutina_idRutina) REFERENCES rutina(idRutina)
);

CREATE TABLE plan_suscripcion (
  idPlan INT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(50) NOT NULL,
  descripcion VARCHAR(255),
  precio_mensual INT NOT NULL,
  duracion_dias INT NOT NULL,
  beneficios TEXT,
  incluye_clases BIT NOT NULL,
  destacado BIT NOT NULL,
  activo BIT NOT NULL,
  PRIMARY KEY (idPlan)
);

CREATE TABLE suscripcion (
  idSuscripcion INT NOT NULL AUTO_INCREMENT,
  idCliente INT NOT NULL,
  idPlan INT NOT NULL,
  fecha_inicio DATE NOT NULL,
  fecha_fin DATE NOT NULL,
  precio_pagado INT NOT NULL,
  estado VARCHAR(20) NOT NULL,
  PRIMARY KEY (idSuscripcion),
  FOREIGN KEY (idCliente) REFERENCES cliente(idCliente),
  FOREIGN KEY (idPlan) REFERENCES plan_suscripcion(idPlan)
);

CREATE TABLE registro_peso (
  idRegistro INT NOT NULL AUTO_INCREMENT,
  idCliente INT NOT NULL,
  fecha DATE NOT NULL,
  peso DOUBLE NOT NULL,
  altura DOUBLE NOT NULL,
  PRIMARY KEY (idRegistro),
  FOREIGN KEY (idCliente) REFERENCES cliente(idCliente)
);

-- Horario de clases, acceso de entrenadores, rutinas por entrenador y avisos de vencimiento
ALTER TABLE clase ADD COLUMN dia_semana VARCHAR(10);
ALTER TABLE entrenador ADD COLUMN password VARCHAR(255);
ALTER TABLE rutina ADD COLUMN Entrenador_idEntrenador INT, ADD FOREIGN KEY (Entrenador_idEntrenador) REFERENCES entrenador(idEntrenador);
ALTER TABLE suscripcion ADD COLUMN aviso_vencimiento_enviado BIT NOT NULL DEFAULT 0;

CREATE TABLE token_recuperacion (
  idToken INT NOT NULL AUTO_INCREMENT,
  token_hash VARCHAR(64) NOT NULL UNIQUE,
  tipo_usuario VARCHAR(12) NOT NULL,
  id_usuario INT NOT NULL,
  expira DATETIME(6) NOT NULL,
  usado BIT NOT NULL,
  PRIMARY KEY (idToken)
);
