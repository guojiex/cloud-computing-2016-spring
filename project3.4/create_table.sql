DROP TABLE IF EXISTS `userinfo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `userinfo`(
    `userid` int NOT NULL,
    `name` varchar(50) NOT NULL,
    `profileurl` varchar(100) NOT NULL,
    PRIMARY KEY (`userid`)  
)DEFAULT CHARSET=utf8mb4; 
/*!40101 SET character_set_client = @saved_cs_client */;

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users`(
    `userid` int NOT NULL,
    `password` varchar(50) NOT NULL,
    PRIMARY KEY (`userid`)  
)DEFAULT CHARSET=utf8mb4; 
/*!40101 SET character_set_client = @saved_cs_client */;
