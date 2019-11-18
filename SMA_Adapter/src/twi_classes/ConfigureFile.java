package twi_classes;

public class ConfigureFile {
	
private static String authConsumerKey="";
private static String authConsumerSecret="";
private static String authAccessToken="";
private static String accessTokenSecret="";
private static long timedelay=6000;
private static long max_tweets=1000000;
private static String[] arrayKeyWords;
public static String getAuthConsumerKey() {
	return authConsumerKey;
}
public static void setAuthConsumerKey(String authConsumerKey) {
	ConfigureFile.authConsumerKey = authConsumerKey;
}
public static String getAuthConsumerSecret() {
	return authConsumerSecret;
}
public static void setAuthConsumerSecret(String authConsumerSecret) {
	ConfigureFile.authConsumerSecret = authConsumerSecret;
}
public static String getAuthAccessToken() {
	return authAccessToken;
}
public static void setAuthAccessToken(String authAccessToken) {
	ConfigureFile.authAccessToken = authAccessToken;
}
public static String getAccessTokenSecret() {
	return accessTokenSecret;
}
public static void setAccessTokenSecret(String accessTokenSecret) {
	ConfigureFile.accessTokenSecret = accessTokenSecret;
}
public static long getTimedelay() {
	return timedelay;
}
public static void setTimedelay(long timedelay) {
	ConfigureFile.timedelay = timedelay;
}
public static long getMax_tweets() {
	return max_tweets;
}
public static void setMax_tweets(long max_tweets) {
	ConfigureFile.max_tweets = max_tweets;
}
public static String[] getArrayKeyWords() {
	return arrayKeyWords;
}
public static void setArrayKeyWords(String[] strings) {
	ConfigureFile.arrayKeyWords = strings;
}
}
