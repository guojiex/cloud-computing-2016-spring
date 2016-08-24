DROP TABLE IF EXISTS `userhash`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `userhash` (
    `useridhash` varchar(200) NOT NULL,
    `rowkey` int NOT NULL
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4; 
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `tweetdata`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tweetdata` (
    `rowkey` int NOT NULL,
    `rawtext` text,
    PRIMARY KEY (rowkey)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4; 
/*!40101 SET character_set_client = @saved_cs_client */;
