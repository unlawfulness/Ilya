import javafx.scene.chart.ScatterChart;
//558
import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;

public class BeatBox implements Serializable {

    JPanel mainPanel;
    ArrayList<JCheckBox> checkBoxList; // Лист для хранения флажков
    Sequencer sequencer;
    Sequence sequence;
    Track track;
    JFrame theFrame;

    String[] instrumentNames = {"Bass Drum",
            "Closed Hi-Hat",
            "Open Hi-Hat",
            "Acoustic Snare",
            "Crash Cymbal",
            "Hand Clap",
            "High Tom",
            "Hi Bongo",
            "Maracas",
            "Whistle",
            "Low Conha",
            "Cowbell",
            "Vibraslap",
            "Low-mid Tom",
            "High Agogo",
            "Open Hi Conga"}; // Массив для хранения названий инструментов
    int[] instruments = {35,42,46,38,49,39,50,60,70,72,64,56,58,47,67,63}; // Номера инструментов

    public static void main(String[] args) {
        new BeatBox().buildGui();
    }

    public void buildGui() {

        theFrame = new JFrame("Cyber BeatBox");
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);
        background.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));// Пустая граница для создания полей между краями панели и компонентами

        checkBoxList = new ArrayList<JCheckBox>();
        Box buttonBox = new Box(BoxLayout.Y_AXIS);//Бокс для расположения кнопок вертикально, добавляем сначала кнопки в бокс, а потом бокс на панель

        JButton start = new JButton("Start");
        start.addActionListener(new MyStartListener());
        buttonBox.add(start);

        JButton stop = new JButton("Stop");
        stop.addActionListener(new MyStopListener());
        buttonBox.add(stop);

        JButton upTempo = new JButton("Tempo Up");
        upTempo.addActionListener(new MyUpTempoListener());
        buttonBox.add(upTempo);

        JButton downTempo = new JButton("Tempo Down");
        downTempo.addActionListener(new MyDownTempoListener());
        buttonBox.add(downTempo);

        JButton dropCheckBoxes =  new JButton("Drop"); // Добавил кнопку сброса флажков
        dropCheckBoxes.addActionListener(new MyDropCheckBoxesListener());
        buttonBox.add(dropCheckBoxes);

        JButton saveProperties =  new JButton("Save"); // Добавил кнопку сброса флажков
        saveProperties.addActionListener(new MySaveListener());
        buttonBox.add(saveProperties);

        JButton loadProperties =  new JButton("Load"); // Добавил кнопку сброса флажков
        loadProperties.addActionListener(new MyLoadListener());
        buttonBox.add(loadProperties);

        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for (int i = 0; i < 16; i++) {
            nameBox.add(new Label(instrumentNames[i]));
        }

        background.add(BorderLayout.EAST,buttonBox);
        background.add(BorderLayout.WEST,nameBox);

        theFrame.getContentPane().add(background);

        GridLayout grid = new GridLayout(16,16);
        grid.setVgap(1);
        grid.setHgap(2);
        mainPanel = new JPanel(grid);
        background.add(BorderLayout.CENTER, mainPanel);

        for (int i = 0; i < 256; i++) {
            JCheckBox c = new JCheckBox();
            c.setSelected(false);
            checkBoxList.add(c);
            mainPanel.add(c);
        }

        setUpMidi();
        theFrame.setSize(600,500);
        theFrame.setVisible(true);

    }//Конец метода buildGui

    public void setUpMidi() {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ,4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);

        } catch (Exception e) {e.printStackTrace();}
    }// Конец метода setUpMidi

    public void buildTrackAndStart(){

        int[] trackList = null;

        sequence.deleteTrack(track);
        track = sequence.createTrack();

        for (int i = 0; i < 16; i++) {
            trackList = new int[16];

            int key = instruments[i];

            for (int j = 0; j < 16; j++) {

                JCheckBox jc = (JCheckBox) checkBoxList.get(j + (16*i));
                if (jc.isSelected()) trackList[j] = key;
                else trackList[j] = 0;

            }//Конец внутреннего цикла

            makeTracks(trackList);
            track.add(makeEvent(176,1,127,0,16));
        }//Конец внешнего цикла

        track.add(makeEvent(192,9,1,0,15));
        try{

            sequencer.setSequence(sequence);
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);
        }catch (Exception e){e.printStackTrace();}
    }//Конец метода buildTrackAndStart

    private class MyStartListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {

            buildTrackAndStart();

        }
    }// Конец метода MyStartListener

    private class MyStopListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {

            sequencer.stop();

        }
    } //Конец метода MyStopListener

    private class MyUpTempoListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {

            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (tempoFactor * 1.02));

        }
    } //Конец метода MyUpTempoListener

    private class MyDownTempoListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {

            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (tempoFactor * 0.98));

        }
    }//Конец метода MyDownTempoListener

    public void makeTracks(int[] list){

        for (int i = 0; i < 16; i++) {
            int key = list[i];

            if (key != 0) {
                track.add(makeEvent(144,9,key,100,i));
                track.add(makeEvent(128,9,key,100,i+1));
            }
        }
    }// Конец метода makeTracks

    public static MidiEvent makeEvent(int command, int channel, int dataOne, int dataTwo, int tick){

        MidiEvent event = null;
        try{
            ShortMessage a = new ShortMessage();
            a.setMessage(command, channel, dataOne, dataTwo);
            event = new MidiEvent(a, tick);
        }catch (Exception e){}

        return event;

    }//Конец метода makeEvent

    private class MyDropCheckBoxesListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            for (int i = 0; i < checkBoxList.size(); i++) {checkBoxList.get(i).setSelected(false);}
        }//Конец метода MyDropCheckBoxesListener
    }

    private class MySaveListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean[] checkboxState = new boolean[256];

            for (int i = 0; i < 256; i++) {

                JCheckBox check = (JCheckBox) checkBoxList.get(i);
                if (check.isSelected()) checkboxState[i] = true;

            }

            try {
                FileOutputStream fs = new FileOutputStream(new File("Checkbox.ser"));
                ObjectOutputStream os = new ObjectOutputStream(fs);
                os.writeObject(checkboxState);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private class MyLoadListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean[] checkBoxState = null;

            try {
                FileInputStream fi = new FileInputStream(new File("Checkbox.ser"));
                ObjectInputStream oi = new ObjectInputStream(fi);
                checkBoxState = (boolean[]) oi.readObject();
            } catch (Exception ex) {ex.printStackTrace();}

            for (int i = 0; i < 256; i++) {
                JCheckBox check = (JCheckBox) checkBoxList.get(i);
                if (checkBoxState[i])
                    check.setSelected(true);
                else
                    check.setSelected(false);
            }
            sequencer.stop();
            buildTrackAndStart();
        }
    }
}//Конец Класса
