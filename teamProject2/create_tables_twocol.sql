DROP TABLE IF EXISTS `q3data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `q3data` (
    `useriddate` bigint NOT NULL,
     `wordcount` text NOT NULL,
    INDEX index_useriddate (useriddate)  
)ENGINE=myisam DEFAULT CHARSET=utf8mb4; 
/*!40101 SET character_set_client = @saved_cs_client */;
