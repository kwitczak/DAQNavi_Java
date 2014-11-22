import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JScrollPane;

import Automation.BDaq.*;


public class test extends JDialog {

	private final JPanel contentPanel = new JPanel();
	
	private JList listView = new JList();
	private DefaultListModel model = new DefaultListModel();
	
	private final int SECTION_AMOUNT = 2;
	private int channelCount = 3;
	private int channelStart = 0;
	private int samplesPerChan = 1024;
	private int ratePerChan = 1000;
	private int intervalCountPerChan = 512;
	
	private double []scaledData = null;
	
	private BufferedAiCtrl bufferedAiCtrl = new BufferedAiCtrl();
	DeviceInformation selected = new DeviceInformation("DemoDevice1");
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			test dialog = new test();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public test() {
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		listView.setModel(model);
		JScrollPane scrollPane = new JScrollPane(listView);
		scrollPane.setBounds(49, 11, 295, 197);
		contentPanel.add(scrollPane);
		
		JButton btnNewButton = new JButton("New button");
		btnNewButton.setBounds(119, 228, 151, 23);
		contentPanel.add(btnNewButton);
		
		if (scaledData == null){
			scaledData = new double[channelCount * samplesPerChan * SECTION_AMOUNT];
		}
		if(scaledData == null){
			JOptionPane.showMessageDialog(this,  "Sorry! Error in asocating Memory...", "Warning MessageBox", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		try {
			bufferedAiCtrl.setSelectedDevice(selected);;
			ScanChannel scanChannel = bufferedAiCtrl.getScanChannel();
			scanChannel.setChannelStart(channelStart);
			scanChannel.setChannelCount(channelCount);
			scanChannel.setIntervalCount(intervalCountPerChan);
			scanChannel.setSamples(samplesPerChan);
			bufferedAiCtrl.getConvertClock().setRate(ratePerChan);
			bufferedAiCtrl.setStreaming(false);
			AnalogChannel[] channels = bufferedAiCtrl.getChannels();
			for(int i = 0; i < channels.length; i++){
				channels[i].setValueRange(ValueRange.V_Neg10To10);
			}
		} catch (Exception e1){
			e1.printStackTrace();
		}
		
		bufferedAiCtrl.Prepare();
		
		bufferedAiCtrl.addStoppedListener(new StoppedEventListener());
		
		btnNewButton.addActionListener(new ButtonGetDataActionListener());;
		addWindowListener(new WindowCloseActionListener());
	}
	
	class ButtonGetDataActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			bufferedAiCtrl.Start();
		}
		
	}
	
	class StoppedEventListener implements BfdAiEventListener{

		@Override
		public void BfdAiEvent(Object sender, BfdAiEventArgs args) {
			// TODO Auto-generated method stub
			bufferedAiCtrl.GetData(args.Count, scaledData);
			
			String str;
			for (int i = 0; i < channelCount; i ++){
				str = "Channel " + i % channelCount + ": " + scaledData[i];
				model.addElement(str);
			}
		}
		
	}
	
	class WindowCloseActionListener extends WindowAdapter{
		public void windowClosing(WindowEvent e){
			if (bufferedAiCtrl != null) {
				bufferedAiCtrl.Cleanup();
			}
		}
	}
}
