import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.ArrayList;

//classe principal do programa
public class Main{
  public static int maxMusicNumber = 170653;
  public static int maxPlaylistSize = 500;
  public static int playlistLength = 0;
  public static int totalComparisons;
  public static int totalMoves;

  public static String[] totalMusicInfo = new String[maxMusicNumber];
  public static Music[] playlist = new Music[maxPlaylistSize];

  public static void main(String[] args){
    MyIO.setCharset("UTF-8");
    String filepath = "/tmp/data.csv";
    try{
      long initialTime = System.currentTimeMillis();
      totalMusicInfo = playlist[0].ler(filepath, maxMusicNumber);
      
      standartInput();
      sort();
      displayPlaylist();

      long afterTime = System.currentTimeMillis();
      long executionTime = afterTime - initialTime;
      
      createLog(executionTime,"690773_countingsort.txt");
    }

    catch(Exception ex){
      MyIO.println(ex.toString());
      ex.printStackTrace();
    }
  }

  public static void displayPlaylist(){
    for(int i = 0; i < playlistLength; i++){
      playlist[i].imprimir();
    }
  }

  public static boolean isSorted(){
    boolean result = true;
    for(int i = 0; i < playlistLength - 1; i++){
      if(playlist[i].getName().compareTo(playlist[i + 1].getName()) > 0){
        result = false;
        i = playlistLength;
      }
    }
    return result;
  }
  
  public static void swap(int a, int b){
    Music aux = playlist[a].clone();
    playlist[a] = playlist[b].clone();
    playlist[b] = aux.clone();
  }
  
  public static boolean decide(Music a, Music b){
    boolean result;
    totalComparisons++;
    if(a.getPopularity() == b.getPopularity())
      result = (a.getName().compareTo(b.getName()) > 0);
    else
      result = false;
    return result;
  }

  //metodo de desempate por meio do insertion sort 
  public static void secondSort(){
    for(int i = 1; i < playlistLength; i++){
      int j = i - 1;
      Music aux = playlist[i];
      while((j >= 0) && decide(playlist[j],aux)){
        playlist[j + 1] = playlist[j].clone();
        totalMoves++;
        j--;
      }
      playlist[j + 1] = aux.clone();
    }
  }

  public static int getBigger(){
    int bigger = 0;
    for(int i = 0; i < playlistLength; i++){
      if(playlist[i].getPopularity() > bigger)
        bigger = playlist[i].getPopularity();
    }
    return bigger;
  }

  public static void copyArray(Music a[], Music b[]){
    for(int i = 0; i < b.length; i++){
      a[i] = b[i].clone();
    }
  }
  
  //implementacao do countingsort para ordenar por popularity
  public static void sort(){
    int[] count = new int[getBigger() + 1];
    Music[] sorted = new Music[playlistLength];

    //populamos o array de contagem com valores iguais a playlist[i]
    for(int i = 0; i < playlistLength; count[playlist[i].getPopularity()]++, i++);
    
    //acumulamos o com o numero de valores menores que count[i]
    for(int i = 1; i < count.length; count[i] += count[i - 1], i++);

    //ordenacao
    for(int i = (playlistLength - 1); i >= 0; sorted[count[playlist[i].getPopularity()] - 1] = playlist[i].clone(), count[playlist[i].getPopularity()]--, i--){
      totalMoves++;
    }

    copyArray(playlist, sorted);
    secondSort();
  }

  public static void printAnswer(boolean answer){
    if(answer == true)
      MyIO.println("SIM");
    else 
      MyIO.println("NAO");
  }

  public static void standartInput(){ 
     //registro de entrada padrao
     String id = new String();
      do{
        id = MyIO.readLine();
        if(id.compareTo("FIM") != 0){
          playlist[playlistLength] = new Music();
          registrar(totalSearchById(id));
          playlistLength++;
        }
      }while(id.compareTo("FIM") != 0);
  }
  
  public static void createLog(long executionTime, String filepath)throws IOException{
    BufferedWriter bw = new BufferedWriter(new FileWriter(filepath));
    bw.write("690773\t" + "\t" + totalComparisons + "\t" +  totalMoves  + "\t" + executionTime);
    bw.close();
  }

  //metodo que procura dentre todas as musicas a que possui determinado id
  public static String totalSearchById(String id){
    String resp = new String();
    for(int i = 0; i < maxMusicNumber; i++){
      if(totalMusicInfo[i].contains(id) == true){ 
        resp = totalMusicInfo[i];
        i = maxMusicNumber;
      }
    }
    return resp;
  }
 
  public static void registrar(String dadosNaoFormatados){
    String[] atributos = dataSplit(dadosNaoFormatados, 19);     //total de 19 atributos lidos porem 2 nao serao registrados
    trataNulidade(atributos); 
    playlist[playlistLength].setValence(Double.parseDouble(atributos[0]));
    playlist[playlistLength].setYear(Integer.parseInt(atributos[1]));
    playlist[playlistLength].setAcousticness(Double.parseDouble(atributos[2]));
    playlist[playlistLength].setArtists(atributos[3]);
    playlist[playlistLength].setDanceability(Double.parseDouble(atributos[4]));
    playlist[playlistLength].setDuration_ms(Integer.parseInt(atributos[5]));
    playlist[playlistLength].setEnergy(Double.parseDouble(atributos[6]));
    //skip explicit
    playlist[playlistLength].setId(atributos[8]);
    playlist[playlistLength].setInstrumentalness(Double.parseDouble(atributos[9]));
    playlist[playlistLength].setKey(atributos[10]);
    playlist[playlistLength].setLiveness(Double.parseDouble(atributos[11]));
    playlist[playlistLength].setLoudness(Double.parseDouble(atributos[12]));
    //skip mode
    playlist[playlistLength].setName(atributos[14]);
    playlist[playlistLength].setPopularity(Integer.parseInt(atributos[15]));
    playlist[playlistLength].setRelease_date(atributos[16]);
    playlist[playlistLength].setSpeechiness(Double.parseDouble(atributos[17]));
    playlist[playlistLength].setTempo(Float.parseFloat(atributos[18]));
  }

  public static void trataNulidade(String[] naoTratado){
    for(int i = 0; i < naoTratado.length; i++){
      if(naoTratado[i] == null)
        naoTratado[i] = "01"; 
    }
  }

  public static String[] dataSplit(String input, int arrayLength){
    String[] output = new String[arrayLength];
    
    //tratamento de virgulas internas e dos artistas
    String aux = input.replace(", ", "##");
    aux = aux.replace("\"[", "[");
    aux = aux.replace("]\"","]");
    aux = aux.replace("\"", ""); 
    output = aux.split(",");
    //retratamento de virgulas
    for(int i = 0; i < arrayLength; i++)
      output[i] = output[i].replace("##", ", ");

    return output;
  }

}
//classe Music
class Music{
  //17 atributos da classe
  private String id;
  private String name;
  private String key;
  private ArrayList<String> artists = new ArrayList<String>();
  private SimpleDate release_date;
  private double acousticness;
  private double danceability;
  private double energy;
  private int duration_ms;
  private double instrumentalness;
  private double valence;
  private int popularity;
  private float tempo;
  private double liveness;
  private double loudness;
  private double speechiness;
  private int year;

  //construtores
  public Music(){

  }

  public Music(String id, String name, String key, String artistList, String release_date_string, double acousticness, 
   double danceability, double energy, int duration_ms, double instrumentalness, double valence,
   int popularity, float tempo, double liveness, double loudness, double speechiness, int year){

    setId(id);
    setName(name);
    setKey(key);
    setArtists(artistList);
    setRelease_date(release_date_string);
    setAcousticness(acousticness);
    setDanceability(danceability);
    setEnergy(energy);
    setDuration_ms(duration_ms);
    setInstrumentalness(instrumentalness);
    setValence(valence);
    setPopularity(popularity);
    setTempo(tempo);
    setLiveness(liveness);
    setLoudness(loudness);
    setSpeechiness(speechiness);
    setYear(year);
  }

  //metodos da classe

  public static String[] ler(String path, int maxMusicNumber)throws IOException{
    String[] musicList = new String[170653];
    int i = 0;
    BufferedReader br = new BufferedReader(new FileReader(path));
    br.readLine(); //primeira linha do arquivo desprezada
    while(i < maxMusicNumber){ 
      musicList[i] = br.readLine();
      i++;
    }
    return musicList;
  } 

 
  public void imprimir(){
    MyIO.print(getId() + " ## " + this.artists + " ## " + this.name + " ## " + release_date.getSimpleDateFormat());
    MyIO.print(" ## " + this.acousticness + " ## " + this.danceability + " ## " + getInstrumentalness() + " ## " + this.liveness);
    MyIO.println(" ## " + this.loudness + " ## " + this.speechiness + " ## " + this.energy + " ## "+ this.duration_ms); 
  }

  public Music clone(){
    Music cloneMusic = new Music();

    //copia de todos os atributos
    cloneMusic.id = this.id;
    cloneMusic.name = this.name;
    cloneMusic.artists = this.artists;
    cloneMusic.release_date = new SimpleDate(this.release_date.getYear() + "-" + this.release_date.getMonth() + "-" + this.release_date.getDay());
    cloneMusic.acousticness = this.acousticness;
    cloneMusic.danceability = this.danceability;
    cloneMusic.energy = this.energy;
    cloneMusic.duration_ms = this.duration_ms;
    cloneMusic.instrumentalness = this.instrumentalness;
    cloneMusic.valence = this.valence;
    cloneMusic.popularity = this.popularity;
    cloneMusic.tempo = this.tempo;
    cloneMusic.liveness = this.liveness;
    cloneMusic.loudness = this.loudness;
    cloneMusic.speechiness = this.speechiness;
    cloneMusic.year = this.year;

    return cloneMusic;
  }

  //metodo auxiliar para a formatacao de artistas
  public String formatArtists(String input){
    String output = new String();
    //tratamento de '
    output = input.replace("['","[");
    output = output.replace("']", "]");
    output = output.replace("',", ",");
    output = output.replace(" '", " ");
    
    //tratamento de []
    output = output.replace("[","");
    output = output.replace("]","");

    return output;
  }

  //metodos getters
  
  public String getId(){
    return this.id;
  }

  public String getName(){
    return this.name;
  }

  public String getKey(){
    return this.key;
  }

  public ArrayList<String> getArtists(){
    return this.artists;
  }
  
  public SimpleDate getRelease_date()
  {
    return this.release_date;
  }
  
  public double getAcousticness(){
    return this.acousticness;
  } 

  public double getDanceability(){
    return this.danceability;
  }

  public double getEnergy(){
    return this.energy;
  }

  public int getDuration_ms(){
    return this.duration_ms;
  }

  public double getInstrumentalness(){
    return this.instrumentalness;
  }

  public double getValence(){
    return this.valence;
  }

  public int getPopularity(){
    return this.popularity;
  }

  public float getTempo(){
    return this.tempo;
  }
  
  public double getLiveness(){
    return this.liveness;
  }

  public double getLoudness(){
    return this.loudness;
  }

  public double getSpeechiness(){
    return this.speechiness;
  }

  public int getYear(){
    return this.year;
  }

  //metodos setters
  
  public void setId(String id){
    this.id = id;
  }

  public void setName(String name){
    this.name = name;
  }

  public void setKey(String key){
    this.key = key;
  }

  public void setArtists(String artistsList){
    artistsList = formatArtists(artistsList);
    String[] splittedList = artistsList.split(", "); 
    for(int i = 0; i < splittedList.length; i++)
      this.artists.add(splittedList[i]); 
  }
  
  public void setRelease_date(String input){
    this.release_date = new SimpleDate(input);
  }
  
  public void setAcousticness(double acousticness){
    this.acousticness = acousticness;
  }

  public void setDanceability(double danceability){
    this.danceability = danceability;
  }

  public void setEnergy(double energy){
    this.energy = energy;
  }

  public void setDuration_ms(int duration_ms){
    this.duration_ms = duration_ms;
  }

  public void setInstrumentalness(double instrumentalness){
    this.instrumentalness = instrumentalness;
  }

  public void setValence(double valence){
    this.valence = valence;
  }

  public void setPopularity(int popularity){
    this.popularity = popularity;
  }

  public void setTempo(float tempo){
    this.tempo =  tempo;
  }

  public void setLiveness(double liveness){
    this.liveness = liveness;
  }

  public void setLoudness(double loudness){
    this.loudness = loudness;
  }

  public void setSpeechiness(double speechiness){
    this.speechiness = speechiness;
  }

  public void setYear(int year){
    this.year = year;
  }
}
//classe criada exclusivamente para simplificar a manipulacao de datas do tp 02
class SimpleDate{
  private int day, month, year;
  
  public SimpleDate(){
    this("0001");
  }

  public SimpleDate(String dateString){
    int[] dateInfos = splitDate(dateString); 
    this.year = dateInfos[0];
    this.month = dateInfos[1];
    this.day = dateInfos[2];
  }
  
  public String getSimpleDateFormat(){
    return (String.format("%02d", getDay()) + "/" + String.format("%02d", getMonth()) + "/" + String.format("%04d",getYear()));
  }

  public static int[] splitDate(String input){
    int[] output = new int[3];

    //apenas ano
    if(input.length() == 4){
      output[0] = Integer.parseInt(input);//ano
      output[1] = 01;//mes 
      output[2] = 01;//dia
    } 
    
    //valores completos
    else{
      String[] auxData = input.split("-");
      output[0] =  Integer.parseInt(auxData[0]);
      output[1] =  Integer.parseInt(auxData[1]);
      output[2] =  Integer.parseInt(auxData[2]);
    }
    return output;
  }

  public int getYear(){
    return this.year;
  }

  public int getMonth(){
    return this.month;
  }

  public int getDay(){
    return this.day;
  }

  public void setYear(int year){
    this.year = year;
  }
  public void setMonth(int month){
    this.month = month;
  }
  public void setDay(int day){
    this.day = day;
  }
}
