package fr.qmn.mamoucalendari.utils;

public class StringLib {
    public String capitalizeFirstLetterOfEachWord(String str) {
        String[] words = str.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String s : words) {
            sb.append(Character.toUpperCase(s.charAt(0)));
            sb.append(s.substring(1));
            sb.append(" ");
        }
        return sb.toString().trim();
    }
}
