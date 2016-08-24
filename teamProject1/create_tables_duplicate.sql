DROP TABLE IF EXISTS `tweetdata`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tweetdata` (
    `useridhash` varchar(50) NOT NULL,
    `rawtext` text NOT NULL,
    INDEX index_useridhash (useridhash)  
)ENGINE=myisam DEFAULT CHARSET=utf8mb4; 
/*!40101 SET character_set_client = @saved_cs_client */;
