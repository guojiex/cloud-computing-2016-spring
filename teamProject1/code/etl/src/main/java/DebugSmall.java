import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DebugSmall {
    public static void main(String[] args){
        //String input="Cari yg baru @pickoneya Balikan / cari yang baru ?\n#PickOneya    PickOneya";
        //String input="RT @leshasmith00: @iadoreumichael you go girl :D";
//        String input="#Pakistán: Asesinada por su familia por casarse sin permiso. En 2013 hubo 869 mujeres víctimas de #crímenesdehonor http://t.co/zfPoGnFUUH Pakistán    crímenesdehonor";
//        Pattern p = Pattern.compile("[^a-zA-Z0-9]");
//        Matcher m = p.matcher(input);
//        int count = 0;
//        int start=0;
//        StringBuilder sb=new StringBuilder();
//        int end=0;
//        while(m.find()) {
//          count++;
//          if(m.start()-start>0)
//              sb.append(input.substring(start,m.start()));
//          sb.append(input.substring(m.start(),m.end()));
//          start=m.end();
//          end=m.end();
//       }
//       if(end<input.length()){
//           sb.append(input.substring(end));
//       }
//       System.out.println(sb.toString());
         System.out.println("\\n");
        System.out.println("\\na".replaceAll("\\\\n", "\n"));
    }
}
