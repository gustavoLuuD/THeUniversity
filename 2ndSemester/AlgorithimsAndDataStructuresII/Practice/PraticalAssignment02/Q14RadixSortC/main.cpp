#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <string.h>
#include <time.h>

#define TOTAL_MUSIC_NUMBER 170625
#define MAX_LINE_LENGTH 1000
#define MAX_PLAYLIST_LENGTH 500
#define MAX_ID_LENGTH 25

typedef struct{
  char list[500];
  int list_length;
}Artists_list;

//struct Date
typedef struct{
  int day;
  int month;
  int year;
}Date;

//struct Music
typedef struct{
  char id[MAX_ID_LENGTH];
  char name[200];
  char key[200];
  Artists_list artists;
  Date release_date;
  double acousticness;
  double danceability;
  double energy;
  int duration_ms;
  double instrumentalness;
  double valence;
  int popularity;
  float tempo;
  double liveness;
  double loudness;
  double speechiness;
  int year;
}Music;


//cabeçalhos das funções
void displayAnswer(bool answer);
void displayPlaylist(Music playlist[], int n);
void createlog(char *filepath, int comp, int mov, double time);
bool isSorted(Music playlist[], int n);
void swap(Music playlist[], int a, int b);
void sort(Music playlist[], int n, int *mov, int *comp);
void ler(char* filepath,char** totalMusicList);
void imprimir(Music *m);
int standartInput(char input[MAX_PLAYLIST_LENGTH][MAX_ID_LENGTH]);
void insertOnPlaylist(int insert_quantity, char idList[MAX_PLAYLIST_LENGTH][MAX_ID_LENGTH], char **totalMusicList, Music playlist[]);
void readAtribute(int *stringIndex, char *infos, char* output);
void cadastra(char data_without_formatation[],Music* m);
void insertDate(Date* d, char* not_formatted_date);
void displayFormattedDate(Date* d);
void insertArtists(Artists_list* a,char* not_formatted_list);

int main(){
  char idList[MAX_PLAYLIST_LENGTH][MAX_ID_LENGTH];//vetor que armazena os ids do input
  int playlist_length;  
  Music playlist[MAX_PLAYLIST_LENGTH];
  int mov = 0, comp = 0;
  clock_t begin, end;
  double execution_time;

  //alocacao do array de strings
  char **totalMusicList; 
  totalMusicList = (char**)malloc(sizeof(char*) * TOTAL_MUSIC_NUMBER);

  //alocacao das strings contidas no array
  for(int i = 0; i < TOTAL_MUSIC_NUMBER; i++)
    totalMusicList[i] = (char*)malloc(sizeof(char*) * MAX_LINE_LENGTH);
   
  ler("/tmp/data.csv", totalMusicList);

  playlist_length = standartInput(idList);
  insertOnPlaylist(playlist_length, idList,totalMusicList, playlist);

  begin = clock(); 
  sort(playlist,playlist_length, &mov, &comp);
  displayPlaylist(playlist, playlist_length);
  end = clock();
  
  execution_time = ((end - begin) / (double)CLOCKS_PER_SEC);
  createlog("690773_radixsort.txt", comp, mov, execution_time);
  
  free(totalMusicList);
  return 0;
}

void displayAnswer(bool answer){
  if(answer == true)
    printf("SIM\n");
  else 
    printf("NAO\n");
}

void displayPlaylist(Music playlist[], int n){
  for(int i = 0; i < n; i++)
    imprimir(&playlist[i]);
}

//funcao para documentacao do algorimto
void createlog(char *filepath,int comp, int mov, double time){
  FILE *fileptr = fopen(filepath, "w");
  fprintf(fileptr, "690773\t%d\t%d\t%f",comp, mov, time); 
  fclose(fileptr);
}

void swap(Music playlist[], int a, int b){
  Music aux = playlist[a];
  playlist[a] = playlist[b];
  playlist[b] = aux;
}

bool isSorted(Music playlist[], int n){
  bool result = true;
  for(int i = 0; i < n - 1; i++){
    if(strcmp(playlist[i].name, playlist[i + 1].name) > 0){
      result = false;
      i = n - 1;
    }
  }
  return result;
}

//funcao auxiliar para cuidar do desempate por nome
bool decide(Music *a, Music *b){
  bool result;
  if(a->duration_ms == b->duration_ms)
    result = (strcmp(a->name, b->name) > 0);
  else 
    result = false;
  return result; 
}

//ordenacao de desempate por nomes 
void handleEqualsDuration(Music playlist[], int n, int *comp, int *mov){
  for(int i = 1; i < n; i++){
    int j = i - 1;
    Music aux = playlist[i];
    while((j >= 0) && decide(&playlist[j], &aux)){
      playlist[j + 1] = playlist[j];
      *mov +=1;
      *comp +=1;
      j++;
    }
    playlist[j + 1] = aux;
    *mov += 1;
  }
}

int getMaxValue(Music playlist[], int n){
  int max = playlist[0].duration_ms;
  for(int i = 1; i < n; i++){
    if(playlist[i].duration_ms > max)
      max = playlist[i].duration_ms;
  }
  return max;
}

void countingSort(Music playlist[], int n, int place, int *comp, int *mov){
  Music output[n + 1];
  int max = (playlist[0].duration_ms / place) % 10;
  for(int i = 1; i < n; i++){
    if(((playlist[i].duration_ms / place) % 10) > max)
      max = playlist[i].duration_ms;
  }

  int count[max + 1];
  //inicializacao do array de contagem
  for(int i = 0; i < max; i++)
    count[i] = 0;
  
  //contagem dos elementos
  for(int i = 0; i < n; i++)
      count[(playlist[i].duration_ms / place) % 10]++;
  
  //acumulando os valores
  for(int i = 1; i < 10; i++)
    count[i] += count[i - 1]; 
  
  //ordenacao
  for(int i = n - 1; i >= 0; i--){
    output[count[(playlist[i].duration_ms / place) % 10] - 1] = playlist[i];
    *mov += 1;
    count[(playlist[i].duration_ms / place) % 10]--;
  }
  
  for(int i = 0; i < n; i++){
    playlist[i] = output[i];
  }
  
  handleEqualsDuration(playlist, n, comp, mov);
}

//funcao que utiliza o radixSort para ordenar pelo atributo duration_ms
void sort(Music playlist[], int n, int* comp, int* mov){
  int max = getMaxValue(playlist, n);
   for(int place = 1; max/place > 0; place *= 10){
    countingSort(playlist, n, place, comp, mov);   
   } 
}

void ler(char* filepath,char** totalMusicList){
  FILE *f = fopen(filepath,"r");
  
  if(f == NULL){
    perror("Error opening file");   //checagem de erro ao abrir o arquivo
    printf("\n Sorry:(\n ");
  }

  else {
    int i = 0;
    char aux[MAX_LINE_LENGTH];
    fgets(aux, MAX_LINE_LENGTH, f);
    while(fgets(aux, MAX_LINE_LENGTH, f) != NULL){
      strcpy(totalMusicList[i], aux);
      i++;
    } 
    fclose(f);
  }
}

//impressao padrao dos elementos relevantes da classe
void imprimir(Music *m){
 printf("%s ## %s ## %s ## ", m->id, m->artists.list, m->name);
 displayFormattedDate(&m->release_date);
 printf(" ## %lg ## %lg ## %lg ## %lg ",m->acousticness, m->danceability, m->instrumentalness, m->liveness);
 printf("## %lg ## %lg ## %lg ## %d \n", m->loudness, m->speechiness, m->energy, m->duration_ms); 
}

//funcao de entrada de dados padronizada
int standartInput(char inputs[MAX_PLAYLIST_LENGTH][MAX_ID_LENGTH]){
  char aux[MAX_ID_LENGTH];
  int input_quantity = 0;
  do{
    scanf(" %s", aux);
    if(strcmp(aux, "FIM")!= 0){
      strcpy(inputs[input_quantity], aux);
      input_quantity++;
    }
  }while(strcmp(aux, "FIM") != 0 && input_quantity <= MAX_PLAYLIST_LENGTH);
  return input_quantity;
}

void insertOnPlaylist(int insert_quantity, char idList[MAX_PLAYLIST_LENGTH][MAX_ID_LENGTH], char **totalMusicList, Music playlist[]){
  void searchById(char id[MAX_ID_LENGTH], char **totalMusicList, char* resp);//cabecalho declarado aqui pois apenas esta funcao chama a searchById
  
  for(int i = 0; i < insert_quantity; i++){
    char music_data[MAX_LINE_LENGTH];
    searchById(idList[i], totalMusicList, music_data); 
    cadastra(music_data, &playlist[i]);
  }
}

//pesquisa sequencial do cadastro
void searchById(char id[MAX_ID_LENGTH], char **totalMusicList, char* resp){
  for(int i = 0; i < TOTAL_MUSIC_NUMBER; i++){
    if(strstr(totalMusicList[i], id) != NULL){
      strcpy(resp, totalMusicList[i]);
      i = TOTAL_MUSIC_NUMBER;
    }
  }
}

//funcao que simula um construtor
void cadastra(char *music_info, Music* m){
  int lineIndex = 0; 
  char fields[19][MAX_LINE_LENGTH];
 
  //split dos atributos 
  for(int i = 0; i < 19; i++)
    readAtribute(&lineIndex, music_info, fields[i]);
  
  //cadastro individual
  m->valence = atof(fields[0]);
  m->year = atoi(fields[1]);
  m->acousticness = atof(fields[2]);
  insertArtists(&m->artists,fields[3]);
  m->danceability = atof(fields[4]);
  m->duration_ms = atof(fields[5]);
  m->energy = atof(fields[6]);
  //explicit nao utilizado
  strcpy(m->id,fields[8]);
  m->instrumentalness = atof(fields[9]);
  strcpy(m->key,fields[10]);
  m->liveness = atof(fields[11]);
  m->loudness = atof(fields[12]);
  //mode nao utilizado
  strcpy(m->name,fields[14]);
  m->popularity = atoi(fields[15]);
  insertDate(&m->release_date, fields[16]);
  m->speechiness = atof(fields[17]);
  m->tempo = atof(fields[18]);
}

//funcao que faz o split dos artibutos corretamente
void readAtribute(int *stringIndex, char *infos, char* output){
  int i = *stringIndex;
  int j = 0;
  while(infos[i] != '\0' && ((infos[i] != ',') || !(infos[i] == ',' && infos[i + 1] != ' '))){ //pulamos virgulas e quotes desnecessarios
    if(infos[i] != '"') 
      output[j++] = infos[i];  
    i++;
  }
   output[j] = '\0';
  *stringIndex = ++i;
}


//funcoes referente a manipulacao de datas  aaaa-mm-dd
void insertDate(Date* d, char* not_formatted_date){
 char day[3];
 char month[3];
 char year[5];
  
 strncpy(year,not_formatted_date,4);
 d->year = atoi(year);
 
 if(not_formatted_date[4] != '-'){
    strcpy(month,"01");
 } 

 else{
    month[0] = not_formatted_date[5];
    month[1] = not_formatted_date[6];
 }

 if(not_formatted_date[7] != '-'){
  strcpy(day,"01");
 }
 else {
    day[0] = not_formatted_date[8];
    day[1] = not_formatted_date[9];
 }
 d->month = atof(month);
 d->year = atof(year);
 d->day = atof(day);
} 

//display que adequa a formatacao de datas
void displayFormattedDate(Date* d){
    printf("%0*d/%0*d/%0*d", 2, d->day, 2, d->month, 4, d->year);
}

void insertArtists(Artists_list* a,char* input){
  int i = 0;
  a->list_length = 0;
  while(i < strlen(input)){
    if(!(input[i] == 39 && (input[i - 1] == 91 || input[i+1] == 93 || input[i + 1] == 44 || input[i - 2] == 44))){
      a->list[a->list_length] = input[i];
      a->list_length++;
    }
    i++;
  }
}
