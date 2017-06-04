package MP;

import java.io.*;
import java.nio.file.Paths;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;

import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;

public class PlayerModel {
	private int numOfFiles;
	private File collectionOfFiles[] = new File[500];
	private File currentSong;
	private Stack<Integer> lastRandomNumber = new Stack<Integer>();
	private MediaPlayer mediaPlayer;
	private int randomNum;
	private double durationInMillisecs;
	//had to instantiate these variables up here so the garbage collector wouldn't erase these and stop the music
	private Media hit;
	private String bip;
	private boolean startLoop = false;
	private boolean firstTime = true;
	private double volumeSetting = .5;
	
	//for some reason mediaPlayer does not have an isPlaying method, so I have to use this boolean
	private boolean isPlaying = false;
	
	
    public PlayerModel() {
    	//this line is required for javafx
    	final JFXPanel fxPanel = new JFXPanel();
    	
    	//purpose of these lines is to set path, and then to add all the songs into a collection which can be referenced later. 
    	File f = new File("C:/Users/Dylan/Desktop/music");
    	int count = 0;
        for (File file : f.listFiles()) {
                if (file.isFile()) {
                	if(file.getName().substring(file.getName().length()-3, file.getName().length()).equals("wav") || file.getName().substring(file.getName().length()-3, file.getName().length()).equals("mp3")){
                		collectionOfFiles[count] = file;
                		count++;
                	}
                }
        }
        numOfFiles = count;
 
    	
	}
    public void setVolume(double percent){
    	if(isFirstTimePlaying()){
    		volumeSetting = percent;
    	}else{
    		volumeSetting = percent;
    		mediaPlayer.setVolume(percent);
    	}
    }
    
    public double getVolume(){
    	return volumeSetting;
    }
    
    //for the ObservableList for songView in FXCollection
    public File[] getFileCollection(){
    	return collectionOfFiles;
    }
    
    
    public double getCurrentTimeOfMusic(){
    	return mediaPlayer.getCurrentTime().toMillis();
    }
    public int getNumOfFiles(){
    	return numOfFiles;
    }
	
	public void play() throws CannotReadException, IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException{
		if(currentSong == null){
			this.shuffle();
		}else if(isPlaying){
			mediaPlayer.pause();
			isPlaying = !isPlaying;
		}else{
			mediaPlayer.play();
			isPlaying = !isPlaying;
		}

	}
	
	//plays specific song when one clicks the listView
	public void playSpecificSong(int indice) throws CannotReadException, IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException{

		if(isPlaying){
			mediaPlayer.stop();
		}
		
		System.out.println("here " + indice);
		currentSong = collectionOfFiles[indice];
		bip = collectionOfFiles[indice].getPath();
		
		hit = new Media(Paths.get(bip).toUri().toString());
		mediaPlayer = new MediaPlayer(hit);
		setVolume(volumeSetting);
		
		findingDurationInSeconds();
		
		mediaPlayer.setStopTime(new Duration(durationInMillisecs));
        
		mediaPlayer.play();
		
		isPlaying = true;
		
	}
	
	public void toggleLoop(){
		startLoop = !startLoop;
		
		//if the loop is now turned on it will set repeat to the current song
		if(startLoop){
			mediaPlayer.setOnRepeat((Runnable) currentSong);
		}
	}
	
	public void shuffle() throws CannotReadException, IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException{
		
		//turns off toggleLoop
		if(startLoop){
			toggleLoop();
		}
		//stops songs from overlapping
		if(isPlaying){
			mediaPlayer.stop();
		}
		//used for remembering the past songs
		lastRandomNumber.add(randomNum);
		randomNum = ThreadLocalRandom.current().nextInt(0, numOfFiles);
		

		
		bip = collectionOfFiles[randomNum].getPath();
		currentSong = collectionOfFiles[randomNum];
		
		hit = new Media(Paths.get(bip).toUri().toString());
		mediaPlayer = new MediaPlayer(hit);
		setVolume(volumeSetting);
		//using JLayer, JAudioTagger, and MP3 Plugin to read mp3 files. Will add WAV functionality. It needs to be here for the getPercentageDone method
		findingDurationInSeconds();
		
        //Had to set stop time for the song because JavaFX could not grab the end time
        mediaPlayer.setStopTime(new Duration(durationInMillisecs));
		        
		mediaPlayer.play();
		
		isPlaying = true;
		


	}
	
	//finding duration in seconds for the runners slider
	private void findingDurationInSeconds() throws CannotReadException, IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException{
		double durationInSeconds;
		AudioFile audioFile = AudioFileIO.read(currentSong);
		durationInSeconds = audioFile.getAudioHeader().getTrackLength();
		durationInMillisecs = durationInSeconds*1000;
	}
	
	public String getCurrentSong(){
		return currentSong.getName();
		
	}
	
	public boolean isSongBackward(){
		if (lastRandomNumber.size() == 0){
			return false;
		}else{
			return true;
		}
	}
	
	public void skipBackward() throws CannotReadException, IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException{
		
		//turns off toggleLoop
		if(startLoop){
			toggleLoop();
		}
		
		if(isPlaying){
			mediaPlayer.stop();
		}
		
		
		//Using MediaPlayer from JavaFX and a stack to get the songs that were already played
		int newCurrentSong = (int) lastRandomNumber.pop();
		bip = collectionOfFiles[newCurrentSong].getPath();
		currentSong = collectionOfFiles[newCurrentSong];
		
	    hit = new Media(new File(bip).toURI().toString());
		mediaPlayer = new MediaPlayer(hit);
		setVolume(volumeSetting);
		//using JLayer, JAudioTagger, and MP3 Plugin to read mp3 files. Will add WAV functionality. It needs to be here for the getPercentageDone method
		findingDurationInSeconds();
        
        //Had to set stop time for the song because JavaFX could not grab the end time
        mediaPlayer.setStopTime(new Duration(durationInMillisecs));
		
		mediaPlayer.play();

		isPlaying = true;
		
	}
	
	public double getPercentageDone(){
		if(isPlaying){
	        
	        return (mediaPlayer.getCurrentTime().toMillis()/mediaPlayer.getStopTime().toMillis())*100;
	        
		}else{
			return 0.0;
		}
	}
	
	public void setPercentageDone(double percentage){
		
		
		
		
		//because theres no setCurrentTime method, I am stopping the song and starting it at a new stop time
		
		//first need to grab the stop time so we know the length of the song
		double stopTime = mediaPlayer.getStopTime().toMillis();
		//now use the percentage to find the current time we want to be at
		double newStartTime = stopTime*(percentage/100);
		
		mediaPlayer.stop();
		isPlaying = false;
		
	    //now to start the mediaPlayer again...
		bip = currentSong.getPath();
		
		hit = new Media(new File(bip).toURI().toString());
		mediaPlayer = new MediaPlayer(hit);
		setVolume(volumeSetting);
		mediaPlayer.setStartTime(new Duration(newStartTime));
		mediaPlayer.play();
		isPlaying = true;
		
		
	}
	
	//Using this so other classes dont have to reference local variables
	public boolean isPlaying(){
		return isPlaying;
	}
	public void setIsPlaying(boolean isPlaying){
		this.isPlaying = isPlaying;
	}
	public boolean isFirstTimePlaying() {
		return firstTime;
	}
	public void setFirstTimePlaying(boolean firstTime) {
		this.firstTime = firstTime;
	}
	
	

}