CREATE DATABASE  IF NOT EXISTS `proyecto_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `proyecto_db`;
-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
--
-- Host: localhost    Database: proyecto_db
-- ------------------------------------------------------
-- Server version	8.4.5

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
-- Table structure for table `auth_group`
--

DROP TABLE IF EXISTS `auth_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `auth_group` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(150) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auth_group`
--

LOCK TABLES `auth_group` WRITE;
/*!40000 ALTER TABLE `auth_group` DISABLE KEYS */;
/*!40000 ALTER TABLE `auth_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `auth_group_permissions`
--

DROP TABLE IF EXISTS `auth_group_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `auth_group_permissions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `group_id` int NOT NULL,
  `permission_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `auth_group_permissions_group_id_permission_id_0cd325b0_uniq` (`group_id`,`permission_id`),
  KEY `auth_group_permissio_permission_id_84c5c92e_fk_auth_perm` (`permission_id`),
  CONSTRAINT `auth_group_permissio_permission_id_84c5c92e_fk_auth_perm` FOREIGN KEY (`permission_id`) REFERENCES `auth_permission` (`id`),
  CONSTRAINT `auth_group_permissions_group_id_b120cbf9_fk_auth_group_id` FOREIGN KEY (`group_id`) REFERENCES `auth_group` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auth_group_permissions`
--

LOCK TABLES `auth_group_permissions` WRITE;
/*!40000 ALTER TABLE `auth_group_permissions` DISABLE KEYS */;
/*!40000 ALTER TABLE `auth_group_permissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `auth_permission`
--

DROP TABLE IF EXISTS `auth_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `auth_permission` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `content_type_id` int NOT NULL,
  `codename` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `auth_permission_content_type_id_codename_01ab375a_uniq` (`content_type_id`,`codename`),
  CONSTRAINT `auth_permission_content_type_id_2f476e4b_fk_django_co` FOREIGN KEY (`content_type_id`) REFERENCES `django_content_type` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auth_permission`
--

LOCK TABLES `auth_permission` WRITE;
/*!40000 ALTER TABLE `auth_permission` DISABLE KEYS */;
INSERT INTO `auth_permission` VALUES (1,'Can add permission',2,'add_permission'),(2,'Can change permission',2,'change_permission'),(3,'Can delete permission',2,'delete_permission'),(4,'Can view permission',2,'view_permission'),(5,'Can add group',1,'add_group'),(6,'Can change group',1,'change_group'),(7,'Can delete group',1,'delete_group'),(8,'Can view group',1,'view_group'),(9,'Can add content type',3,'add_contenttype'),(10,'Can change content type',3,'change_contenttype'),(11,'Can delete content type',3,'delete_contenttype'),(12,'Can view content type',3,'view_contenttype'),(13,'Can add session',4,'add_session'),(14,'Can change session',4,'change_session'),(15,'Can delete session',4,'delete_session'),(16,'Can view session',4,'view_session'),(17,'Can add blacklisted token',5,'add_blacklistedtoken'),(18,'Can change blacklisted token',5,'change_blacklistedtoken'),(19,'Can delete blacklisted token',5,'delete_blacklistedtoken'),(20,'Can view blacklisted token',5,'view_blacklistedtoken'),(21,'Can add outstanding token',6,'add_outstandingtoken'),(22,'Can change outstanding token',6,'change_outstandingtoken'),(23,'Can delete outstanding token',6,'delete_outstandingtoken'),(24,'Can view outstanding token',6,'view_outstandingtoken'),(25,'Can add usuario',7,'add_usuario'),(26,'Can change usuario',7,'change_usuario'),(27,'Can delete usuario',7,'delete_usuario'),(28,'Can view usuario',7,'view_usuario'),(29,'Can add inscripcion',8,'add_inscripcion'),(30,'Can change inscripcion',8,'change_inscripcion'),(31,'Can delete inscripcion',8,'delete_inscripcion'),(32,'Can view inscripcion',8,'view_inscripcion'),(33,'Can add campania',9,'add_campania'),(34,'Can change campania',9,'change_campania'),(35,'Can delete campania',9,'delete_campania'),(36,'Can view campania',9,'view_campania'),(37,'Can add centro salud',10,'add_centrosalud'),(38,'Can change centro salud',10,'change_centrosalud'),(39,'Can delete centro salud',10,'delete_centrosalud'),(40,'Can view centro salud',10,'view_centrosalud'),(41,'Can add contacto',11,'add_contacto'),(42,'Can change contacto',11,'change_contacto'),(43,'Can delete contacto',11,'delete_contacto'),(44,'Can view contacto',11,'view_contacto');
/*!40000 ALTER TABLE `auth_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `campanias`
--

DROP TABLE IF EXISTS `campanias`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `campanias` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `titulo` varchar(100) NOT NULL,
  `descripcion` varchar(1500) NOT NULL,
  `ubicacion` varchar(100) NOT NULL,
  `fecha_inicio` date NOT NULL,
  `fecha_fin` date NOT NULL,
  `estado_campania` varchar(12) NOT NULL,
  `centro_salud_id` bigint DEFAULT NULL,
  `cupo_maximo` int unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `campanias_centro_salud_id_2c119918_fk_centros_salud_id` (`centro_salud_id`),
  CONSTRAINT `campanias_centro_salud_id_2c119918_fk_centros_salud_id` FOREIGN KEY (`centro_salud_id`) REFERENCES `centros_salud` (`id`),
  CONSTRAINT `campanias_chk_1` CHECK ((`cupo_maximo` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `campanias`
--

LOCK TABLES `campanias` WRITE;
/*!40000 ALTER TABLE `campanias` DISABLE KEYS */;
INSERT INTO `campanias` VALUES (1,'Donación de Sangre Hospital Central','Campaña solidaria para pacientes en cirugías y emergencias críticas.','Hospital Central Córdoba','2026-06-10','2026-06-15','Activa',NULL,NULL),(2,'Jornada Donación Plaza Central','Evento abierto para donantes voluntarios de todos los grupos sanguíneos.','Plaza Central Córdoba','2026-12-01','2026-12-02','Proximamente',NULL,NULL),(3,'Maratón de Donación Universitaria','Evento organizado por universidades para fomentar la donación voluntaria.','Ciudad Universitaria Córdoba','2026-11-01','2026-11-05','Proximamente',NULL,NULL),(4,'Campaña Emergencia Pediátrica','Donación urgente para niños en terapia intensiva.','Hospital de Niños','2026-06-12','2026-06-14','Activa',NULL,NULL),(5,'Donación en Empresas Tech','Campaña en empresas de tecnología para concientizar sobre donación.','Parque Empresarial Córdoba','2026-06-25','2026-06-27','Proximamente',NULL,NULL),(6,'Campaña Día Mundial del Donante de Sangre - Córdoba','Evento masivo de donación de sangre con participación ciudadana.','Plaza San Martín','2026-06-08','2026-06-15','Activa',NULL,NULL),(7,'Jornada Solidaria Barrio Güemes','Recolección de sangre para hospitales públicos de la ciudad.','Centro Cultural Güemes','2026-09-23','2026-09-29','Proximamente',11,60),(8,'Campaña para gente','Oh, no tengo sangre!','algun lugar','2026-09-09','2026-09-12','Activa',24,5);
/*!40000 ALTER TABLE `campanias` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `centros_salud`
--

DROP TABLE IF EXISTS `centros_salud`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `centros_salud` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `direccion` varchar(200) NOT NULL,
  `barrio` varchar(50) NOT NULL,
  `localidad` varchar(50) NOT NULL,
  `telefono` varchar(10) DEFAULT NULL,
  `sitio_web` varchar(200) DEFAULT NULL,
  `latitud` decimal(10,7) NOT NULL,
  `longitud` decimal(10,7) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `centros_salud`
--

LOCK TABLES `centros_salud` WRITE;
/*!40000 ALTER TABLE `centros_salud` DISABLE KEYS */;
INSERT INTO `centros_salud` VALUES (1,'Banco Central de Sangre de la Provincia de Córdoba','Rosario de Santa Fe 374','Centro','Córdoba','3512480189','https://ministeriodesalud.cba.gov.ar/banco-de-sangre/',-31.4177671,-64.1792522),(2,'Banco de Sangre Municipal','Salta 480','Centro','Córdoba','3514276240','https://cordoba.gob.ar/donar-sangre-puede-salvar-hasta-cuatro-vidas/',-31.4121493,-64.1763496),(3,'Banco de Sangre de la Universidad Nacional de Córdoba','Enfermera Gordillo Gómez s/n, entre Bv. de la Reforma y Av. Valparaíso','Ciudad Universitaria','Córdoba','3514334121','https://bancodesangre.turnos.unc.edu.ar/',-31.4379571,-64.1878064),(4,'Fundación Banco Central de Sangre','Caseros 1576','Quinta Santa Ana','Córdoba','3514807373','https://www.donarencordoba.com.ar/',-31.4113956,-64.2059516),(5,'Hospital Dr. Arturo Illia - Servicio de Hemoterapia','Av. del Libertador 1450','Cafferata','Alta Gracia','3547429282','https://ministeriodesalud.cba.gov.ar/hospitales-y-centros-de-salud/',-31.6591224,-64.4161881),(6,'Hospital Dr. José Antonio Ceballos - Servicio de Hemoterapia','Gerónimo del Barco 1300','Bell Ville','Bell Ville','3534421003','https://ministeriodesalud.cba.gov.ar/hospitales-y-centros-de-salud/',-32.6168221,-62.7037998),(7,'Hospital Dr. Pedro Vella - Servicio de Hemoterapia','Rosario 300','Corral de Bustos','Corral de Bustos','3468433974','https://ministeriodesalud.cba.gov.ar/hospitales-y-centros-de-salud/',-33.2835410,-62.1939697),(8,'Hospital Aurelio Crespo - Servicio de Hemoterapia','Félix Cáceres s/n','Cruz del Eje','Cruz del Eje','3549426747','https://ministeriodesalud.cba.gov.ar/hospitales-y-centros-de-salud/',-30.7437265,-64.7874548),(9,'Hospital Dr. Ernesto Romagosa - Servicio de Hemoterapia','Colón 247','Deán Funes','Deán Funes','3521479579','https://ministeriodesalud.cba.gov.ar/hospitales-y-centros-de-salud/',-30.4240135,-64.3563512),(10,'Hospital René Favaloro - Servicio de Hemoterapia','Uruguay 537','Huinca Renancó','Huinca Renancó','2336494107','https://ministeriodesalud.cba.gov.ar/hospitales-y-centros-de-salud/',-34.8323721,-64.3756257),(11,'Hospital Vicente Agüero - Servicio de Hemoterapia','España 121','Vicente Agüero','Jesús María','3525426703','https://ministeriodesalud.cba.gov.ar/hospitales-y-centros-de-salud/',-30.9769867,-64.0900763),(12,'Hospital San Antonio - Servicio de Hemoterapia','Enrique Gauna 1251','La Carlota','La Carlota','3584422295','https://ministeriodesalud.cba.gov.ar/hospitales-y-centros-de-salud/',-33.4240280,-63.3033394),(13,'Hospital Ramón J. Cárcano - Servicio de Hemoterapia','Av. Juan Domingo Perón 20','La Maitena','Laboulaye','3385453242','https://ministeriodesalud.cba.gov.ar/hospitales-y-centros-de-salud/',-34.1405091,-63.3920740),(14,'Hospital Abel Ayerza - Servicio de Hemoterapia','Belgrano 350','Marcos Juárez','Marcos Juárez','3472422820','https://ministeriodesalud.cba.gov.ar/hospitales-y-centros-de-salud/',-32.6963534,-62.0963009),(15,'Hospital Dr. Luis M. Bellodi - Servicio de Hemoterapia','Av. Rossel 1800','San Sebastián','Mina Clavero',NULL,'https://ministeriodesalud.cba.gov.ar/hospitales-y-centros-de-salud/',-31.7433298,-64.9991184),(16,'Nuevo Hospital San Antonio de Padua - Servicio de Hemoterapia','Guardias Nacionales 1051','San Antonio de Padua','Río Cuarto','3584678700','https://ministeriodesalud.cba.gov.ar/hospitales-y-centros-de-salud/',-33.1098399,-64.3556313),(17,'Nuevo Hospital Provincial Brig. Gral. Juan Bautista Bustos - Servicio de Hemoterapia','Estanislao del Campo y Amado Nervo','Parque Industrial Leonardo Da Vinci','Río Tercero','3571410210','https://ministeriodesalud.cba.gov.ar/hospitales-y-centros-de-salud/',-32.1849803,-64.1365493),(18,'Hospital J. B. Iturraspe - Medicina Transfusional','Dominga Cullen 450','Parque','San Francisco','3564443722','https://ministeriodesalud.cba.gov.ar/hospitales-y-centros-de-salud/',-31.4279816,-62.0679246),(19,'Hospital Eva Perón - Banco de Sangre','Ruta Provincial 5 km 90, esquina Dr. David Bustos','Santa Rosa de Calamuchita','Santa Rosa de Calamuchita','3546426671','https://ministeriodesalud.cba.gov.ar/hospitales-y-centros-de-salud/',-32.0627076,-64.5304128),(20,'Hospital Dr. Ramón B. Mestre - Servicio de Hemoterapia','Moisés Quinteros 548','Villa Santa Rosa','Villa Santa Rosa de Río Primero','3574480914','https://ministeriodesalud.cba.gov.ar/hospitales-y-centros-de-salud/',-31.1572030,-63.4087868),(21,'Hospital José M. Urrutia - Servicio de Hemoterapia','3 de Febrero 324','Unquillo','Unquillo','8005554141','https://ministeriodesalud.cba.gov.ar/hospitales-y-centros-de-salud/',-31.2346069,-64.3206411),(22,'Hospital Domingo Funes - Servicio de Hemoterapia','Av. Domingo Funes s/n','Villa Caeiro','Santa María de Punilla','3541489676','https://ministeriodesalud.cba.gov.ar/hospitales-y-centros-de-salud/',-31.2967838,-64.4535742),(23,'Hospital Provincial de Villa Dolores - Servicio de Hemoterapia','Av. Belgrano 1800','Villa Dolores','Villa Dolores','3544426437','https://ministeriodesalud.cba.gov.ar/hospitales-y-centros-de-salud/',-31.9498789,-65.1677567),(24,'Hospital Pasteur - Servicio de Hemoterapia','Aldo Serrano esquina Buchardo','Ramón Carrillo','Villa María','3534619138','https://ministeriodesalud.cba.gov.ar/hospitales-y-centros-de-salud/',-32.3939013,-63.2589614),(25,'Hospital San Vicente de Paul - Servicio de Hemoterapia','Bv. Sobremonte 550','Del Molino','Villa del Rosario','3573424704','https://ministeriodesalud.cba.gov.ar/hospitales-y-centros-de-salud/',-31.5630144,-63.5417909);
/*!40000 ALTER TABLE `centros_salud` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `contactos`
--

DROP TABLE IF EXISTS `contactos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contactos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(254) NOT NULL,
  `asunto` varchar(100) NOT NULL,
  `mensaje` varchar(500) NOT NULL,
  `tracked` tinyint(1) NOT NULL,
  `fecha_creacion` datetime(6) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contactos`
--

LOCK TABLES `contactos` WRITE;
/*!40000 ALTER TABLE `contactos` DISABLE KEYS */;
INSERT INTO `contactos` VALUES (1,'juana@gmail.com','ME QUEDÉ SIN SANGREEE','Ayudaaaaa',1,'2026-09-09 18:34:43.009009');
/*!40000 ALTER TABLE `contactos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `django_content_type`
--

DROP TABLE IF EXISTS `django_content_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `django_content_type` (
  `id` int NOT NULL AUTO_INCREMENT,
  `app_label` varchar(100) NOT NULL,
  `model` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `django_content_type_app_label_model_76bd3d3b_uniq` (`app_label`,`model`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `django_content_type`
--

LOCK TABLES `django_content_type` WRITE;
/*!40000 ALTER TABLE `django_content_type` DISABLE KEYS */;
INSERT INTO `django_content_type` VALUES (1,'auth','group'),(2,'auth','permission'),(9,'campanias','campania'),(10,'centros_salud','centrosalud'),(11,'contactos','contacto'),(3,'contenttypes','contenttype'),(8,'inscripciones','inscripcion'),(4,'sessions','session'),(5,'token_blacklist','blacklistedtoken'),(6,'token_blacklist','outstandingtoken'),(7,'usuarios','usuario');
/*!40000 ALTER TABLE `django_content_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `django_migrations`
--

DROP TABLE IF EXISTS `django_migrations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `django_migrations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `app` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `applied` datetime(6) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `django_migrations`
--

LOCK TABLES `django_migrations` WRITE;
/*!40000 ALTER TABLE `django_migrations` DISABLE KEYS */;
INSERT INTO `django_migrations` VALUES (1,'contenttypes','0001_initial','2026-09-09 17:51:15.222561'),(2,'contenttypes','0002_remove_content_type_name','2026-09-09 17:51:16.720316'),(3,'auth','0001_initial','2026-09-09 17:51:21.248036'),(4,'auth','0002_alter_permission_name_max_length','2026-09-09 17:51:22.321401'),(5,'auth','0003_alter_user_email_max_length','2026-09-09 17:51:22.377726'),(6,'auth','0004_alter_user_username_opts','2026-09-09 17:51:22.465962'),(7,'auth','0005_alter_user_last_login_null','2026-09-09 17:51:22.590890'),(8,'auth','0006_require_contenttypes_0002','2026-09-09 17:51:22.644145'),(9,'auth','0007_alter_validators_add_error_messages','2026-09-09 17:51:22.708174'),(10,'auth','0008_alter_user_username_max_length','2026-09-09 17:51:22.774012'),(11,'auth','0009_alter_user_last_name_max_length','2026-09-09 17:51:22.841811'),(12,'auth','0010_alter_group_name_max_length','2026-09-09 17:51:23.036777'),(13,'auth','0011_update_proxy_permissions','2026-09-09 17:51:23.118944'),(14,'auth','0012_alter_user_first_name_max_length','2026-09-09 17:51:23.197643'),(15,'centros_salud','0001_initial','2026-09-09 17:51:23.698774'),(16,'centros_salud','0002_cargar_centros','2026-09-09 17:51:23.838130'),(17,'campanias','0001_initial','2026-09-09 17:51:25.761063'),(18,'campanias','0002_convertir_estado_a_choice','2026-09-09 17:51:28.788696'),(19,'campanias','0003_alter_campania_table','2026-09-09 17:51:29.023919'),(20,'campanias','0004_campania_centro_salud','2026-09-09 17:51:30.017511'),(21,'campanias','0005_campania_cupo_maximo','2026-09-09 17:51:31.086951'),(22,'centros_salud','0003_ajustar_longitudes','2026-09-09 17:51:36.753932'),(23,'contactos','0001_initial','2026-09-09 17:51:37.263618'),(24,'usuarios','0001_initial','2026-09-09 17:51:47.801717'),(25,'inscripciones','0001_initial','2026-09-09 17:51:49.218422'),(26,'inscripciones','0002_initial','2026-09-09 17:51:50.309788'),(27,'inscripciones','0003_alter_inscripcion_table','2026-09-09 17:51:50.604289'),(28,'inscripciones','0004_inscripcion_unica_usuario_campania','2026-09-09 17:51:51.002864'),(29,'sessions','0001_initial','2026-09-09 17:51:51.555860'),(30,'token_blacklist','0001_initial','2026-09-09 17:51:54.345857'),(31,'token_blacklist','0002_outstandingtoken_jti_hex','2026-09-09 17:51:55.589497'),(32,'token_blacklist','0003_auto_20171017_2007','2026-09-09 17:51:55.726023'),(33,'token_blacklist','0004_auto_20171017_2013','2026-09-09 17:51:57.088763'),(34,'token_blacklist','0005_remove_outstandingtoken_jti','2026-09-09 17:51:58.180850'),(35,'token_blacklist','0006_auto_20171017_2113','2026-09-09 17:51:58.459466'),(36,'token_blacklist','0007_auto_20171017_2214','2026-09-09 17:52:01.508257'),(37,'token_blacklist','0008_migrate_to_bigautofield','2026-09-09 17:52:07.271357'),(38,'token_blacklist','0010_fix_migrate_to_bigautofield','2026-09-09 17:52:07.338971'),(39,'token_blacklist','0011_linearizes_history','2026-09-09 17:52:07.403895'),(40,'token_blacklist','0012_alter_outstandingtoken_user','2026-09-09 17:52:07.460808'),(41,'usuarios','0002_alter_usuario_email','2026-09-09 17:52:07.774139'),(42,'usuarios','0003_convertir_rol_y_grupo_a_choices','2026-09-09 17:52:20.997571'),(43,'usuarios','0004_alter_usuario_options_alter_usuario_apellido_and_more','2026-09-09 17:52:22.411146'),(44,'usuarios','0005_usuario_fecha_nacimiento','2026-09-09 17:52:23.798113');
/*!40000 ALTER TABLE `django_migrations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `django_session`
--

DROP TABLE IF EXISTS `django_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `django_session` (
  `session_key` varchar(40) NOT NULL,
  `session_data` longtext NOT NULL,
  `expire_date` datetime(6) NOT NULL,
  PRIMARY KEY (`session_key`),
  KEY `django_session_expire_date_a5c62663` (`expire_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `django_session`
--

LOCK TABLES `django_session` WRITE;
/*!40000 ALTER TABLE `django_session` DISABLE KEYS */;
/*!40000 ALTER TABLE `django_session` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inscripciones`
--

DROP TABLE IF EXISTS `inscripciones`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inscripciones` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `fecha_inscripcion` datetime(6) NOT NULL,
  `campania_id` bigint NOT NULL,
  `usuario_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `inscripcion_unica_usuario_campania` (`usuario_id`,`campania_id`),
  KEY `inscripciones_inscripcion_campania_id_bda42df4_fk_campanias_id` (`campania_id`),
  CONSTRAINT `inscripciones_inscri_usuario_id_481272be_fk_usuarios_` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`),
  CONSTRAINT `inscripciones_inscripcion_campania_id_bda42df4_fk_campanias_id` FOREIGN KEY (`campania_id`) REFERENCES `campanias` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inscripciones`
--

LOCK TABLES `inscripciones` WRITE;
/*!40000 ALTER TABLE `inscripciones` DISABLE KEYS */;
INSERT INTO `inscripciones` VALUES (1,'2026-09-09 18:39:26.353691',3,2),(2,'2026-09-09 18:39:59.798340',7,2),(3,'2026-09-09 18:40:05.092573',2,2),(4,'2026-09-09 18:40:12.492222',8,2);
/*!40000 ALTER TABLE `inscripciones` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `token_blacklist_blacklistedtoken`
--

DROP TABLE IF EXISTS `token_blacklist_blacklistedtoken`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `token_blacklist_blacklistedtoken` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `blacklisted_at` datetime(6) NOT NULL,
  `token_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `token_id` (`token_id`),
  CONSTRAINT `token_blacklist_blacklistedtoken_token_id_3cc7fe56_fk` FOREIGN KEY (`token_id`) REFERENCES `token_blacklist_outstandingtoken` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `token_blacklist_blacklistedtoken`
--

LOCK TABLES `token_blacklist_blacklistedtoken` WRITE;
/*!40000 ALTER TABLE `token_blacklist_blacklistedtoken` DISABLE KEYS */;
/*!40000 ALTER TABLE `token_blacklist_blacklistedtoken` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `token_blacklist_outstandingtoken`
--

DROP TABLE IF EXISTS `token_blacklist_outstandingtoken`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `token_blacklist_outstandingtoken` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `token` longtext NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `expires_at` datetime(6) NOT NULL,
  `user_id` bigint DEFAULT NULL,
  `jti` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `token_blacklist_outstandingtoken_jti_hex_d9bdf6f7_uniq` (`jti`),
  KEY `token_blacklist_outs_user_id_83bc629a_fk_usuarios_` (`user_id`),
  CONSTRAINT `token_blacklist_outs_user_id_83bc629a_fk_usuarios_` FOREIGN KEY (`user_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `token_blacklist_outstandingtoken`
--

LOCK TABLES `token_blacklist_outstandingtoken` WRITE;
/*!40000 ALTER TABLE `token_blacklist_outstandingtoken` DISABLE KEYS */;
INSERT INTO `token_blacklist_outstandingtoken` VALUES (1,'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsImV4cCI6MTc4OTA2NTEwMywiaWF0IjoxNzg4OTc4NzAzLCJqdGkiOiJiNWU5Zjc2MzhhYWU0ZDZhOTM3YjJmYmI5NjU3Y2U5ZCIsInVzZXJfaWQiOjF9.Fm7fXGqWsb8qMBtonU_5qAq9fbNXF8Jqqa767aXQ7O8','2026-09-09 18:31:43.503010','2026-09-10 18:31:43.000000',1,'b5e9f7638aae4d6a937b2fbb9657ce9d'),(2,'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsImV4cCI6MTc4OTA2NTEwNiwiaWF0IjoxNzg4OTc4NzA2LCJqdGkiOiJjMmMwMGEzZTcyMjg0MDk5YWU2NWQ0YmYyY2MzNjcwZCIsInVzZXJfaWQiOjF9.vA3JysKY2WuA9y1JH5L9WZEe9UXkl1f9FMqoSFmD4kY','2026-09-09 18:31:46.690302','2026-09-10 18:31:46.000000',1,'c2c00a3e72284099ae65d4bf2cc3670d'),(3,'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsImV4cCI6MTc4OTA2NTI4OCwiaWF0IjoxNzg4OTc4ODg4LCJqdGkiOiIzNWQwMjk0NTMxY2M0ZjY0YTI1ZmQ5YmExMjFlYzBhOSIsInVzZXJfaWQiOjF9.g5sOj8jRvsC0wQaunOypoYL_CoYLi_E5idzTbBIx_R0','2026-09-09 18:34:48.136426','2026-09-10 18:34:48.000000',1,'35d0294531cc4f64a25fd9ba121ec0a9'),(4,'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsImV4cCI6MTc4OTA2NTUzOCwiaWF0IjoxNzg4OTc5MTM4LCJqdGkiOiI4OWUwYjE3YWY4ZGY0NzliYmYwZDYwOWRiOGZkZGY4OCIsInVzZXJfaWQiOjJ9.694ZRURfKBfndi30pFePnASok3_-4cveueCvK96NIOA','2026-09-09 18:38:58.329503','2026-09-10 18:38:58.000000',2,'89e0b17af8df479bbf0d609db8fddf88');
/*!40000 ALTER TABLE `token_blacklist_outstandingtoken` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `password` varchar(128) NOT NULL,
  `last_login` datetime(6) DEFAULT NULL,
  `is_superuser` tinyint(1) NOT NULL,
  `username` varchar(150) NOT NULL,
  `first_name` varchar(150) NOT NULL,
  `last_name` varchar(150) NOT NULL,
  `email` varchar(254) NOT NULL,
  `is_staff` tinyint(1) NOT NULL,
  `is_active` tinyint(1) NOT NULL,
  `date_joined` datetime(6) NOT NULL,
  `dni` varchar(8) NOT NULL,
  `nombre` varchar(25) NOT NULL,
  `apellido` varchar(25) NOT NULL,
  `fecha_registro` date NOT NULL,
  `rol` varchar(16) NOT NULL,
  `grupo_sanguineo` varchar(3) NOT NULL,
  `fecha_nacimiento` date NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `dni` (`dni`),
  UNIQUE KEY `usuarios_usuario_email_0a82e5f9_uniq` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuarios`
--

LOCK TABLES `usuarios` WRITE;
/*!40000 ALTER TABLE `usuarios` DISABLE KEYS */;
INSERT INTO `usuarios` VALUES (1,'pbkdf2_sha256$1200000$ViB8flsiyIE5XpjLwY0StQ$ziCtZoy6+71i4tJDJZio7CtOmdj4OCt+HzzpAOTaXp0=',NULL,0,'vcat','Vainilla','Cat','vainillacat@mail.com',1,1,'2026-09-09 15:30:19.000000','12345678','Vainilla','Cat','2026-09-09','Administrador','O+','2007-04-23'),(2,'pbkdf2_sha256$1200000$WHwy5HB3Czsjoqiixb2rC6$OVawf8rHuKUVm+YUT60Jr7H5oRkoNJY+0/8JqYSksZI=',NULL,0,'Byron','','','lordbyron@gmail.com',0,1,'2026-09-09 18:38:38.559831','13252132','Byron','Lord','2026-09-09','Usuario Estandar','A+','2001-03-21'),(3,'pbkdf2_sha256$1200000$ahklDHIupGyHiDyt3TT2Ea$KDpgyFf1GHVuOuzlC82NIIW/AgWMnaLAVn4M5KK6Md0=',NULL,1,'admin@sangreya.com','Administrador','SangreYa','admin@sangreya.com',1,1,'2026-09-13 17:12:21.000000','99999999','Administrador','SangreYa','2026-09-13','Administrador','O+','2000-01-01');
/*!40000 ALTER TABLE `usuarios` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuarios_groups`
--

DROP TABLE IF EXISTS `usuarios_groups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios_groups` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `usuario_id` bigint NOT NULL,
  `group_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `usuarios_usuario_groups_usuario_id_group_id_4ed5b09e_uniq` (`usuario_id`,`group_id`),
  KEY `usuarios_usuario_groups_group_id_e77f6dcf_fk_auth_group_id` (`group_id`),
  CONSTRAINT `usuarios_usuario_gro_usuario_id_7a34077f_fk_usuarios_` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`),
  CONSTRAINT `usuarios_usuario_groups_group_id_e77f6dcf_fk_auth_group_id` FOREIGN KEY (`group_id`) REFERENCES `auth_group` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuarios_groups`
--

LOCK TABLES `usuarios_groups` WRITE;
/*!40000 ALTER TABLE `usuarios_groups` DISABLE KEYS */;
/*!40000 ALTER TABLE `usuarios_groups` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuarios_user_permissions`
--

DROP TABLE IF EXISTS `usuarios_user_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios_user_permissions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `usuario_id` bigint NOT NULL,
  `permission_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `usuarios_usuario_user_pe_usuario_id_permission_id_217cadcd_uniq` (`usuario_id`,`permission_id`),
  KEY `usuarios_usuario_use_permission_id_4e5c0f2f_fk_auth_perm` (`permission_id`),
  CONSTRAINT `usuarios_usuario_use_permission_id_4e5c0f2f_fk_auth_perm` FOREIGN KEY (`permission_id`) REFERENCES `auth_permission` (`id`),
  CONSTRAINT `usuarios_usuario_use_usuario_id_60aeea80_fk_usuarios_` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuarios_user_permissions`
--

LOCK TABLES `usuarios_user_permissions` WRITE;
/*!40000 ALTER TABLE `usuarios_user_permissions` DISABLE KEYS */;
/*!40000 ALTER TABLE `usuarios_user_permissions` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-13 17:20:36
