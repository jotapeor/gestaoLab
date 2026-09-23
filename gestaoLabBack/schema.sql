CREATE DATABASE  IF NOT EXISTS `gestaolab_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `gestaolab_db`;
-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: gestaolab_db
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `curso_setor`
--

DROP TABLE IF EXISTS `curso_setor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `curso_setor` (
  `id_curso_setor` bigint NOT NULL AUTO_INCREMENT,
  `nome` varchar(150) NOT NULL,
  `tipo` enum('CURSO','SETOR') NOT NULL DEFAULT 'CURSO',
  `ativo` tinyint(1) NOT NULL DEFAULT '1',
  `data_cadastro` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_curso_setor`),
  UNIQUE KEY `uk_curso_setor_nome` (`nome`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `curso_setor`
--

LOCK TABLES `curso_setor` WRITE;
/*!40000 ALTER TABLE `curso_setor` DISABLE KEYS */;
/*!40000 ALTER TABLE `curso_setor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `equipamento`
--

DROP TABLE IF EXISTS `equipamento`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `equipamento` (
  `id_equipamento` bigint NOT NULL AUTO_INCREMENT,
  `id_laboratorio` bigint NOT NULL,
  `nome` varchar(150) NOT NULL,
  `patrimonio` varchar(50) DEFAULT NULL,
  `marca` varchar(100) DEFAULT NULL,
  `modelo` varchar(100) DEFAULT NULL,
  `numero_serie` varchar(100) DEFAULT NULL,
  `status` enum('DISPONIVEL','EM_USO','EM_MANUTENCAO') NOT NULL DEFAULT 'DISPONIVEL',
  `foto_path` varchar(255) DEFAULT NULL,
  `observacoes` text,
  `ativo` tinyint(1) NOT NULL DEFAULT '1',
  `data_cadastro` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_equipamento`),
  UNIQUE KEY `uk_equipamento_patrimonio` (`patrimonio`),
  KEY `idx_equipamento_laboratorio` (`id_laboratorio`),
  CONSTRAINT `fk_equipamento_laboratorio` FOREIGN KEY (`id_laboratorio`) REFERENCES `laboratorio` (`id_laboratorio`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `equipamento`
--

LOCK TABLES `equipamento` WRITE;
/*!40000 ALTER TABLE `equipamento` DISABLE KEYS */;
/*!40000 ALTER TABLE `equipamento` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `laboratorio`
--

DROP TABLE IF EXISTS `laboratorio`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `laboratorio` (
  `id_laboratorio` bigint NOT NULL AUTO_INCREMENT,
  `nome` varchar(150) NOT NULL,
  `localizacao` varchar(255) DEFAULT NULL,
  `capacidade` int DEFAULT NULL,
  `ativo` tinyint(1) NOT NULL DEFAULT '1',
  `data_cadastro` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_laboratorio`),
  CONSTRAINT `ck_laboratorio_capacidade` CHECK (((`capacidade` is null) or (`capacidade` > 0)))
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `laboratorio`
--

LOCK TABLES `laboratorio` WRITE;
/*!40000 ALTER TABLE `laboratorio` DISABLE KEYS */;
INSERT INTO `laboratorio` VALUES (1,'Laboratório Principal',NULL,NULL,1,'2026-09-22 21:53:22');
/*!40000 ALTER TABLE `laboratorio` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `material`
--

DROP TABLE IF EXISTS `material`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `material` (
  `id_material` bigint NOT NULL AUTO_INCREMENT,
  `id_laboratorio` bigint NOT NULL,
  `nome` varchar(150) NOT NULL,
  `descricao` text,
  `tipo` enum('CONSUMIVEL','REUTILIZAVEL') NOT NULL,
  `unidade_medida` varchar(20) NOT NULL DEFAULT 'un',
  `quantidade_disponivel` decimal(10,2) NOT NULL DEFAULT '0.00',
  `quantidade_minima` decimal(10,2) DEFAULT NULL,
  `localizacao` varchar(150) DEFAULT NULL,
  `ativo` tinyint(1) NOT NULL DEFAULT '1',
  `data_cadastro` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_material`),
  KEY `idx_material_laboratorio` (`id_laboratorio`),
  CONSTRAINT `fk_material_laboratorio` FOREIGN KEY (`id_laboratorio`) REFERENCES `laboratorio` (`id_laboratorio`) ON DELETE RESTRICT,
  CONSTRAINT `ck_material_quantidade` CHECK ((`quantidade_disponivel` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `material`
--

LOCK TABLES `material` WRITE;
/*!40000 ALTER TABLE `material` DISABLE KEYS */;
/*!40000 ALTER TABLE `material` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `movimentacao_material`
--

DROP TABLE IF EXISTS `movimentacao_material`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `movimentacao_material` (
  `id_movimentacao` bigint NOT NULL AUTO_INCREMENT,
  `id_material` bigint NOT NULL,
  `id_usuario` bigint NOT NULL,
  `id_registrado_por` bigint DEFAULT NULL,
  `id_utilizacao` bigint DEFAULT NULL,
  `id_movimentacao_origem` bigint DEFAULT NULL,
  `tipo` enum('ENTRADA','RETIRADA','DEVOLUCAO','DANO') NOT NULL,
  `quantidade` decimal(10,2) NOT NULL,
  `observacao` text,
  `data_movimentacao` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_movimentacao`),
  KEY `idx_mov_material_data` (`id_material`,`data_movimentacao`),
  KEY `idx_mov_usuario` (`id_usuario`),
  KEY `idx_mov_registrado_por` (`id_registrado_por`),
  KEY `idx_mov_utilizacao` (`id_utilizacao`),
  KEY `idx_mov_origem` (`id_movimentacao_origem`),
  KEY `idx_mov_tipo_data` (`tipo`,`data_movimentacao`),
  CONSTRAINT `fk_mov_material` FOREIGN KEY (`id_material`) REFERENCES `material` (`id_material`) ON DELETE RESTRICT,
  CONSTRAINT `fk_mov_origem` FOREIGN KEY (`id_movimentacao_origem`) REFERENCES `movimentacao_material` (`id_movimentacao`) ON DELETE RESTRICT,
  CONSTRAINT `fk_mov_registrado_por` FOREIGN KEY (`id_registrado_por`) REFERENCES `usuario` (`id_usuario`) ON DELETE SET NULL,
  CONSTRAINT `fk_mov_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE RESTRICT,
  CONSTRAINT `fk_mov_utilizacao` FOREIGN KEY (`id_utilizacao`) REFERENCES `utilizacao_laboratorio` (`id_utilizacao`) ON DELETE SET NULL,
  CONSTRAINT `ck_mov_quantidade` CHECK ((`quantidade` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `movimentacao_material`
--

LOCK TABLES `movimentacao_material` WRITE;
/*!40000 ALTER TABLE `movimentacao_material` DISABLE KEYS */;
/*!40000 ALTER TABLE `movimentacao_material` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ocorrencia_equipamento`
--

DROP TABLE IF EXISTS `ocorrencia_equipamento`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ocorrencia_equipamento` (
  `id_ocorrencia` bigint NOT NULL AUTO_INCREMENT,
  `id_equipamento` bigint NOT NULL,
  `id_usuario` bigint NOT NULL,
  `tipo` enum('PROBLEMA','MANUTENCAO_PREVENTIVA','MANUTENCAO_CORRETIVA') NOT NULL,
  `descricao` text NOT NULL,
  `status` enum('ABERTA','EM_ANDAMENTO','RESOLVIDA') NOT NULL DEFAULT 'ABERTA',
  `solucao` text,
  `data_abertura` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `data_resolucao` datetime DEFAULT NULL,
  PRIMARY KEY (`id_ocorrencia`),
  KEY `idx_ocorrencia_equipamento` (`id_equipamento`,`data_abertura`),
  KEY `idx_ocorrencia_usuario` (`id_usuario`),
  CONSTRAINT `fk_ocorrencia_equipamento` FOREIGN KEY (`id_equipamento`) REFERENCES `equipamento` (`id_equipamento`) ON DELETE RESTRICT,
  CONSTRAINT `fk_ocorrencia_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE RESTRICT,
  CONSTRAINT `ck_ocorrencia_periodo` CHECK (((`data_resolucao` is null) or (`data_resolucao` >= `data_abertura`)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ocorrencia_equipamento`
--

LOCK TABLES `ocorrencia_equipamento` WRITE;
/*!40000 ALTER TABLE `ocorrencia_equipamento` DISABLE KEYS */;
/*!40000 ALTER TABLE `ocorrencia_equipamento` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `projeto`
--

DROP TABLE IF EXISTS `projeto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `projeto` (
  `id_projeto` bigint NOT NULL AUTO_INCREMENT,
  `titulo` varchar(255) NOT NULL,
  `tipo` enum('TCC_I','TCC_II','EXTENSAO','MONITORIA','BOLSISTA','PESQUISA','AULA','OUTRO') NOT NULL,
  `id_orientador` bigint DEFAULT NULL,
  `descricao` text,
  `ativo` tinyint(1) NOT NULL DEFAULT '1',
  `data_cadastro` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_projeto`),
  KEY `idx_projeto_orientador` (`id_orientador`),
  CONSTRAINT `fk_projeto_orientador` FOREIGN KEY (`id_orientador`) REFERENCES `usuario` (`id_usuario`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `projeto`
--

LOCK TABLES `projeto` WRITE;
/*!40000 ALTER TABLE `projeto` DISABLE KEYS */;
/*!40000 ALTER TABLE `projeto` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reserva_equipamento`
--

DROP TABLE IF EXISTS `reserva_equipamento`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reserva_equipamento` (
  `id_reserva_equipamento` bigint NOT NULL AUTO_INCREMENT,
  `id_equipamento` bigint NOT NULL,
  `id_usuario` bigint NOT NULL,
  `id_projeto` bigint DEFAULT NULL,
  `data_inicio` datetime NOT NULL,
  `data_fim` datetime NOT NULL,
  `motivo` text,
  `status` enum('PENDENTE','CONFIRMADA','CANCELADA','CONCLUIDA') NOT NULL DEFAULT 'CONFIRMADA',
  `data_criacao` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_reserva_equipamento`),
  KEY `idx_reserva_eq_periodo` (`id_equipamento`,`data_inicio`,`data_fim`),
  KEY `idx_reserva_eq_usuario` (`id_usuario`),
  KEY `idx_reserva_eq_projeto` (`id_projeto`),
  CONSTRAINT `fk_reserva_eq_equipamento` FOREIGN KEY (`id_equipamento`) REFERENCES `equipamento` (`id_equipamento`) ON DELETE RESTRICT,
  CONSTRAINT `fk_reserva_eq_projeto` FOREIGN KEY (`id_projeto`) REFERENCES `projeto` (`id_projeto`) ON DELETE SET NULL,
  CONSTRAINT `fk_reserva_eq_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE RESTRICT,
  CONSTRAINT `ck_reserva_eq_periodo` CHECK ((`data_fim` > `data_inicio`))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reserva_equipamento`
--

LOCK TABLES `reserva_equipamento` WRITE;
/*!40000 ALTER TABLE `reserva_equipamento` DISABLE KEYS */;
/*!40000 ALTER TABLE `reserva_equipamento` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reserva_laboratorio`
--

DROP TABLE IF EXISTS `reserva_laboratorio`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reserva_laboratorio` (
  `id_reserva` bigint NOT NULL AUTO_INCREMENT,
  `id_laboratorio` bigint NOT NULL,
  `id_usuario` bigint NOT NULL,
  `id_projeto` bigint DEFAULT NULL,
  `data_inicio` datetime NOT NULL,
  `data_fim` datetime NOT NULL,
  `motivo` text,
  `status` enum('PENDENTE','CONFIRMADA','CANCELADA','CONCLUIDA') NOT NULL DEFAULT 'CONFIRMADA',
  `data_criacao` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_reserva`),
  KEY `idx_reserva_lab_periodo` (`id_laboratorio`,`data_inicio`,`data_fim`),
  KEY `idx_reserva_lab_usuario` (`id_usuario`),
  KEY `idx_reserva_lab_projeto` (`id_projeto`),
  CONSTRAINT `fk_reserva_lab_laboratorio` FOREIGN KEY (`id_laboratorio`) REFERENCES `laboratorio` (`id_laboratorio`) ON DELETE RESTRICT,
  CONSTRAINT `fk_reserva_lab_projeto` FOREIGN KEY (`id_projeto`) REFERENCES `projeto` (`id_projeto`) ON DELETE SET NULL,
  CONSTRAINT `fk_reserva_lab_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE RESTRICT,
  CONSTRAINT `ck_reserva_lab_periodo` CHECK ((`data_fim` > `data_inicio`))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reserva_laboratorio`
--

LOCK TABLES `reserva_laboratorio` WRITE;
/*!40000 ALTER TABLE `reserva_laboratorio` DISABLE KEYS */;
/*!40000 ALTER TABLE `reserva_laboratorio` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `id_usuario` bigint NOT NULL AUTO_INCREMENT,
  `nome` varchar(150) NOT NULL,
  `matricula` varchar(50) DEFAULT NULL,
  `email` varchar(150) NOT NULL,
  `senha` varchar(255) NOT NULL,
  `perfil` enum('COORDENADOR','PROFESSOR','USUARIO') NOT NULL DEFAULT 'USUARIO',
  `id_curso_setor` bigint DEFAULT NULL,
  `id_responsavel` bigint DEFAULT NULL,
  `ativo` tinyint(1) NOT NULL DEFAULT '1',
  `primeiro_acesso` tinyint(1) NOT NULL DEFAULT '1',
  `foto_path` varchar(255) DEFAULT NULL,
  `data_criacao` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `uk_usuario_email` (`email`),
  UNIQUE KEY `uk_usuario_matricula` (`matricula`),
  KEY `idx_usuario_curso_setor` (`id_curso_setor`),
  KEY `idx_usuario_responsavel` (`id_responsavel`),
  CONSTRAINT `fk_usuario_curso_setor` FOREIGN KEY (`id_curso_setor`) REFERENCES `curso_setor` (`id_curso_setor`) ON DELETE SET NULL,
  CONSTRAINT `fk_usuario_responsavel` FOREIGN KEY (`id_responsavel`) REFERENCES `usuario` (`id_usuario`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario`
--

LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
INSERT INTO `usuario` VALUES (1,'Administrador',NULL,'admin@gestaolab.local','$2a$10$V94.yc6BXs8lLqdQHv18eOMPBD.hSWtvkj6S6jWie1dRVC7TuUSH6','COORDENADOR',NULL,NULL,1,1,NULL,'2026-09-22 21:53:22');
/*!40000 ALTER TABLE `usuario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario_projeto`
--

DROP TABLE IF EXISTS `usuario_projeto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario_projeto` (
  `id_usuario` bigint NOT NULL,
  `id_projeto` bigint NOT NULL,
  PRIMARY KEY (`id_usuario`,`id_projeto`),
  KEY `idx_usuario_projeto_projeto` (`id_projeto`),
  CONSTRAINT `fk_usuario_projeto_projeto` FOREIGN KEY (`id_projeto`) REFERENCES `projeto` (`id_projeto`) ON DELETE CASCADE,
  CONSTRAINT `fk_usuario_projeto_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario_projeto`
--

LOCK TABLES `usuario_projeto` WRITE;
/*!40000 ALTER TABLE `usuario_projeto` DISABLE KEYS */;
/*!40000 ALTER TABLE `usuario_projeto` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `utilizacao_equipamento`
--

DROP TABLE IF EXISTS `utilizacao_equipamento`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `utilizacao_equipamento` (
  `id_utilizacao_equipamento` bigint NOT NULL AUTO_INCREMENT,
  `id_equipamento` bigint NOT NULL,
  `id_usuario` bigint NOT NULL,
  `id_reserva_equipamento` bigint DEFAULT NULL,
  `id_utilizacao` bigint DEFAULT NULL,
  `data_inicio` datetime NOT NULL,
  `data_fim` datetime DEFAULT NULL,
  `duracao_minutos` int GENERATED ALWAYS AS (timestampdiff(MINUTE,`data_inicio`,`data_fim`)) STORED,
  `observacoes` text,
  PRIMARY KEY (`id_utilizacao_equipamento`),
  KEY `idx_util_eq_equipamento` (`id_equipamento`,`data_inicio`),
  KEY `idx_util_eq_usuario` (`id_usuario`),
  KEY `idx_util_eq_reserva` (`id_reserva_equipamento`),
  KEY `idx_util_eq_utilizacao` (`id_utilizacao`),
  CONSTRAINT `fk_util_eq_equipamento` FOREIGN KEY (`id_equipamento`) REFERENCES `equipamento` (`id_equipamento`) ON DELETE RESTRICT,
  CONSTRAINT `fk_util_eq_reserva` FOREIGN KEY (`id_reserva_equipamento`) REFERENCES `reserva_equipamento` (`id_reserva_equipamento`) ON DELETE SET NULL,
  CONSTRAINT `fk_util_eq_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE RESTRICT,
  CONSTRAINT `fk_util_eq_utilizacao` FOREIGN KEY (`id_utilizacao`) REFERENCES `utilizacao_laboratorio` (`id_utilizacao`) ON DELETE SET NULL,
  CONSTRAINT `ck_util_eq_periodo` CHECK (((`data_fim` is null) or (`data_fim` >= `data_inicio`)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `utilizacao_equipamento`
--

LOCK TABLES `utilizacao_equipamento` WRITE;
/*!40000 ALTER TABLE `utilizacao_equipamento` DISABLE KEYS */;
/*!40000 ALTER TABLE `utilizacao_equipamento` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `utilizacao_laboratorio`
--

DROP TABLE IF EXISTS `utilizacao_laboratorio`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `utilizacao_laboratorio` (
  `id_utilizacao` bigint NOT NULL AUTO_INCREMENT,
  `id_laboratorio` bigint NOT NULL,
  `id_usuario` bigint NOT NULL,
  `id_reserva` bigint DEFAULT NULL,
  `id_projeto` bigint DEFAULT NULL,
  `data_entrada` datetime NOT NULL,
  `data_saida` datetime DEFAULT NULL,
  `duracao_minutos` int GENERATED ALWAYS AS (timestampdiff(MINUTE,`data_entrada`,`data_saida`)) STORED,
  `atividade_realizada` text,
  `observacoes` text,
  PRIMARY KEY (`id_utilizacao`),
  KEY `idx_utilizacao_lab_entrada` (`id_laboratorio`,`data_entrada`),
  KEY `idx_utilizacao_lab_usuario` (`id_usuario`,`data_entrada`),
  KEY `idx_utilizacao_lab_reserva` (`id_reserva`),
  KEY `idx_utilizacao_lab_projeto` (`id_projeto`),
  CONSTRAINT `fk_utilizacao_lab_laboratorio` FOREIGN KEY (`id_laboratorio`) REFERENCES `laboratorio` (`id_laboratorio`) ON DELETE RESTRICT,
  CONSTRAINT `fk_utilizacao_lab_projeto` FOREIGN KEY (`id_projeto`) REFERENCES `projeto` (`id_projeto`) ON DELETE SET NULL,
  CONSTRAINT `fk_utilizacao_lab_reserva` FOREIGN KEY (`id_reserva`) REFERENCES `reserva_laboratorio` (`id_reserva`) ON DELETE SET NULL,
  CONSTRAINT `fk_utilizacao_lab_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE RESTRICT,
  CONSTRAINT `ck_utilizacao_lab_periodo` CHECK (((`data_saida` is null) or (`data_saida` >= `data_entrada`)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `utilizacao_laboratorio`
--

LOCK TABLES `utilizacao_laboratorio` WRITE;
/*!40000 ALTER TABLE `utilizacao_laboratorio` DISABLE KEYS */;
/*!40000 ALTER TABLE `utilizacao_laboratorio` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-22 22:10:54
