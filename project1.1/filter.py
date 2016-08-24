#!/usr/bin/python
# -*- coding: UTF-8 -*-
'''
    In this python program I first filter the whole dataset output to 
    a file named 'output'.
    And within this process of filtering, I also get all the answers
    for question 1-9, and print them out(so that it can be use by redirection to
    another answer file)
'''
import re
from operator import itemgetter, attrgetter 
access_time=0
movie=0
answer_7=0
answer_5=0
film_2014 = 0
film_2015 = 0
answer_8=0        
'''the judge function of answer5'''
def question_5(line):
    line=line.lower()
    cloud=line.find("cloud")
    computing=line.find("computing")
    # if there is no cloud and computing inside the title then return false
    if cloud==-1 or computing==-1:
        return False
    '''
    before cloud if there is something, then it should be end with some not alpha char
    between cloud and computing, if there is something, the left and right edges must be not alpha chars
    if there is something after computing, it should be start with not alpha char
    '''
    if re.search(r'(.*\W?)?cloud(\W?(.*\W?)?)?computing(\W?.*)?',line) or re.search(r'(.*\W?)?computing(\W?(.*\W?)?)?cloud(\W?.*)?',line):
        return True
    #otherwise it is not answer
    return False
'''the filter function of the whole dataset'''
def myfilter(ori_line):
    global access_time,movie,answer_7,answer_5,answer_8,film_2014,film_2015
    #split the whole line using space
    line=ori_line.split()
    dict={}
    #rule 0
    if len(line)!=4:
        #answer_2
        access_time+=int(line[1])
        return None    
    #answer_2
    access_time=access_time+int(line[2])

    #rule 1
    if not line[0]=='en':
        return None

    #rule 2
    exclude_list=["Media:","Special:","Talk:","User:",
            "User_talk:","Project:","Project_talk:","File:","File_talk:","MediaWiki:",
            "MediaWiki_talk:","Template:","Template_talk:","Help:","Help_talk:",
            "Category:","Category_talk:","Portal:","Wikipedia:","Wikipedia_talk:"]
    for str in exclude_list:
        if line[1].startswith(str):
            return None

    #rule 3
    if (line[1][0].isalpha())and(line[1][0].islower()):
        return None

    #rule 4
    extension_list=[".jpg", ".gif", ".png", ".JPG", ".GIF", ".PNG", ".txt", ".ico"]
    for str in extension_list:
        if line[1].endswith(str):
            return None

    #rule 5
    special_list=[r"404_error/","Main_Page","Hypertext_Transfer_Protocol","Search"]
    for str in special_list:
        if line[1]==str:
            return None

    '''after filtered'''
    #answer_6,find the title with film and with the most access
    if not line[1].find("film")==-1:
        if int(line[2])>movie:
            movie=int(line[2])
    #answer_7
    if 2500< int(line[2]) and int(line[2])<3000:
        answer_7=answer_7+1
    #answer_9,count the access time for 2014_film and 2015_film
    if not line[1].find("2014_film")==-1:
        film_2014 = film_2014 +int(line[2])
    if not line[1].find("2015_film")==-1:
        film_2015 =film_2015+int(line[2])
    #answer_8
    if re.match(r'\d[a-zA-Z]', line[1]):
        answer_8=answer_8+int(line[2])
    #answer_5,see if the title have cloud and computing
    if question_5(line[1]):
        answer_5+=1

    return line[1],line[2]#"{0}\t{1}".format(line[1],line[2])

input_file=open("pagecounts-20151201-000000")
output_file=open("output","w")
emerge=0
result={}
totalLines=0
while 1:
    line=input_file.readline()
    if not line:
        break
    else:
        #compute the total line number
        totalLines=totalLines+1
        dict=myfilter(line)
        #if the line is not fullfill the rules
        if dict==None:
            continue
        else:
            #count the lines remain after filter
            emerge=emerge+1
            result[dict[0]]=int(dict[1])#for sorting
            
result=sorted(result.iteritems(), key=itemgetter(1), reverse=True) 
for pairs in result:
    output_file.write("{0}\t{1}\n".format(pairs[0],pairs[1]))

print(str(totalLines))#answer_1
print(str(access_time))#answer_2
print(str(emerge))#answer_3
print(result[0][0])#answer_4
print(str(answer_5))#answer_5
print(str(movie))#answer_6
print(str(answer_7))#answer_7
print(str(answer_8))#answer_8
if film_2014 >= film_2015 :#answer_9
    print("2014")
else:
    print("2015")
input_file.close()
output_file.close()
