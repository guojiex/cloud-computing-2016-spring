DROP TABLE IF EXISTS `tweetdata`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tweetdata` (
    `tweetid` varchar(30) NOT NULL CHARACTER SET utf8mb4,
    `hashtags` varchar(40)  NOT NULL CHARACTER SET utf8mb4 ,
    `tweetdate` varchar(40)  NOT NULL CHARACTER SET utf8mb4,
  `userid` int NOT NULL,
  `rawtext` text CHARACTER SET utf8mb4
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4; 
/*!40101 SET character_set_client = @saved_cs_client */;
