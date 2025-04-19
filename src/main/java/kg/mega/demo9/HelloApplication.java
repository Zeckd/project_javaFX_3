package kg.mega.demo9;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javafx.application.Platform;


public class HelloApplication extends Application {




        private ArrayList<File> songs;
        private ArrayList<String> songNames = new ArrayList<>();
        private int id = 0;
        private MediaPlayer mediaPlayer;
        private boolean isPlaying = false;

        @Override
        public void start(Stage stage) {
            VBox root = new VBox();
            Button openButton = new Button("Choose Folder");
            ListView<String> playList = new ListView<>();
            Label currentSongText = new Label("Current Song:");
            Label nameTrack = new Label("Track");
            Slider progressSlider = new Slider();
            Label timeTrack = new Label("00:00 / 00:00");
            HBox controls = new HBox();
            Button prevButton = new Button("Prev");
            Button playButton = new Button("Play");
            Button pauseButton = new Button("Pause");
            Button stopButton = new Button("Stop");
            Button nextButton = new Button("Next");
            CheckBox repeatCheck = new CheckBox("Repeat");
            CheckBox shuffleCheck = new CheckBox("Shuffle");


            controls.getChildren().addAll(prevButton, playButton, pauseButton, stopButton,
                    nextButton, repeatCheck, shuffleCheck);
            root.getChildren().addAll(openButton, playList, currentSongText, nameTrack,
                    progressSlider, timeTrack, controls);
            root.setFocusTraversable(true);
            root.requestFocus();

            openButton.setOnAction(e -> {
                DirectoryChooser chooser = new DirectoryChooser();
                File directory = chooser.showDialog(stage);
                if (directory != null) {
                    songs = new ArrayList<>();
                    songNames.clear();
                    File[] files = directory.listFiles(file -> file.getName().endsWith(".mp3")
                            || file.getName().endsWith(".wav"));
                    if (files != null) {
                        for (File file : files) {
                            songs.add(file);
                            songNames.add(file.getName());
                        }
                        playList.getItems().setAll(songNames);
                    }
                }
            });
            progressSlider.setOnMouseReleased(e -> {
                if (mediaPlayer != null) {
                    mediaPlayer.seek(Duration.seconds(progressSlider.getValue()));
                }
            });

            playList.getSelectionModel().selectedIndexProperty().addListener
                    ((obs, oldVal, newVal) -> {
                if (newVal != null && newVal.intValue() >= 0) {
                    id = newVal.intValue();
                    isPlaying = false;
                }
            });

            playButton.setOnAction(e -> playTrack(nameTrack, timeTrack, progressSlider, playList));
            pauseButton.setOnAction(e -> {
                isPlaying = true;
                if (mediaPlayer != null) {
                    mediaPlayer.pause();
                }
            });
            stopButton.setOnAction(e -> {
                isPlaying = false;
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                }
            });
            prevButton.setOnAction(e -> {
                if (songs == null || songs.isEmpty()) return;
                id = (id != 0) ? id - 1 : songs.size() - 1;
                playTrack(nameTrack, timeTrack, progressSlider, playList);
            });
            nextButton.setOnAction(e -> {
                if (songs == null || songs.isEmpty()) return;
                id = (id != songs.size() - 1) ? id + 1 : 0;
                playTrack(nameTrack, timeTrack, progressSlider, playList);
            });


            progressSlider.valueChangingProperty().addListener
                    ((obs, wasChanging, isChanging) -> {
                if (!isChanging && mediaPlayer != null) {
                    mediaPlayer.seek(Duration.seconds(progressSlider.getValue()));
                }
                mediaPlayer.setOnEndOfMedia(() -> {
                    if (repeatCheck.isSelected()) {
                        mediaPlayer.seek(Duration.ZERO);
                        mediaPlayer.play();
                    } else {
                        playTrack(nameTrack, timeTrack, progressSlider, playList);
                    }
                });
                if (shuffleCheck.isSelected()) {
                    id = new Random().nextInt(songs.size());
                } else {
                    id = (id != songs.size() - 1) ? id + 1 : 0;
                }

            });

            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                if (mediaPlayer != null) {
                    progressSlider.setValue(mediaPlayer.getCurrentTime().toSeconds());
                    timeTrack.setText(formatDuration(mediaPlayer.getCurrentTime())
                            + " / "
                            + formatDuration(mediaPlayer.getTotalDuration()));
                }
            }));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();

            Scene scene = new Scene(root, 900, 600);
            scene.setOnKeyPressed(event -> {
                switch (event.getCode()) {
                    case SPACE:
                        if (mediaPlayer != null) {
                            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                                mediaPlayer.pause();
                                isPlaying = false;
                            } else if (mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED || mediaPlayer.getStatus() == MediaPlayer.Status.STOPPED) {
                                mediaPlayer.play();
                                isPlaying = true;
                            }
                        }
                        break;
                    case RIGHT:
                        if (mediaPlayer != null) {
                            mediaPlayer.seek(mediaPlayer.getCurrentTime().add(Duration.seconds(5)));
                        }
                        break;
                    case LEFT:
                        if (mediaPlayer != null) {
                            mediaPlayer.seek(mediaPlayer.getCurrentTime().subtract(Duration.seconds(5)));
                        }
                        break;
                }
            });

            Platform.runLater(() -> scene.getRoot().requestFocus());

            stage.setScene(scene);
            stage.show();
            scene.getRoot().requestFocus();

        }

        private void playTrack(Label nameTrack, Label timeTrack, Slider progressSlider, ListView<String> playList) {
            if (songs == null || songs.isEmpty()) return;

            if (isPlaying) {
                if (mediaPlayer != null) mediaPlayer.play();
                return;
            }

            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }

            File currentSong = songs.get(id);
            nameTrack.setText(currentSong.getName());
            Media media = new Media(currentSong.toURI().toString());
            mediaPlayer = new MediaPlayer(media);

            mediaPlayer.setOnReady(() -> {
                Duration duration = mediaPlayer.getTotalDuration();
                progressSlider.setMax(duration.toSeconds());
                progressSlider.setValue(0);
                timeTrack.setText("00:00 / " + formatDuration(duration));

            });

            mediaPlayer.play();
            playList.getSelectionModel().select(id);
        }

        private static String formatDuration(Duration duration) {
            int minutes = (int) duration.toMinutes();
            int seconds = (int) duration.toSeconds() % 60 ;
            return String.format("%02d:%02d", minutes, seconds);
        }

        public static void main(String[] args) {
            launch();
        }
    }
