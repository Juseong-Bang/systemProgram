package sicXeSimulator;

import java.io.File;

import javax.swing.*;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;

import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;

import javax.swing.border.LineBorder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class VisualSimulator extends JFrame {
	private JTextField InputFileName;
	private JTextField HprogramName;
	private JTextField HprogramAdr;
	private JTextField HprogramLen;
	private JTextField RegSW;
	private JTextField DecRegA;
	private JTextField HexRegA;
	private JTextField DecRegX;
	private JTextField HexRegX;
	private JTextField DecRegL;
	private JTextField HexRegL;
	private JTextField DecRegPC;
	private JTextField HexRegPC;
	private JTextField DecRegB;
	private JTextField HexRegB;
	private JTextField DecRegS;
	private JTextField HexRegS;
	private JTextField DecRegT;
	private JTextField HexRegT;
	private JTextField RegF;
	private JTextField UsingDev;
	private JTextField EFirstInstAdr;
	private JTextField TarAdr;
	private JTextField StAdr;
	private JMenuBar menuBar;
	private JButton FileOpenButton;
	private JButton oneStepBtn;
	private JMenuItem FileMenu;
	private JButton AllStepBtn;
	private JButton QuitBtn;
	private ResourceManager rMgr;
	private SicSimulator sicSim;
	public JTextArea LogText;
	public JTextArea InstText;

	public VisualSimulator() {

		menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		LogText = new JTextArea();
		FileOpenButton = new JButton("open");
		oneStepBtn = new JButton("Exe (1step)");
		oneStepBtn.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				oneStep();
			}
		});
		oneStepBtn.setEnabled(false);
		FileMenu = new JMenuItem("File");
		AllStepBtn = new JButton("Exe (All)");
		AllStepBtn.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				allStep();
			}
		});
		AllStepBtn.setEnabled(false);;
		QuitBtn = new JButton("QUIT");
		QuitBtn.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				System.exit(0);

			}
		});
		FileMenu.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent arg0) {

			}
		});
		FileMenu.setHorizontalAlignment(SwingConstants.LEFT);
		menuBar.add(FileMenu);

		JMenuItem AboutMenu = new JMenuItem("About");
		AboutMenu.setHorizontalAlignment(SwingConstants.LEFT);
		menuBar.add(AboutMenu);
		getContentPane().setLayout(null);

		JPanel panel = new JPanel();
		panel.setBounds(12, 0, 376, 33);
		getContentPane().add(panel);

		JLabel lblFilename = new JLabel("FileName: ");
		panel.add(lblFilename);

		InputFileName = new JTextField();
		InputFileName.setFont(new Font("Gulim", Font.PLAIN, 12));
		panel.add(InputFileName);
		InputFileName.setColumns(12);

		FileOpenButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				FileDialog fd = new FileDialog(new Frame(), "objectCode File", FileDialog.LOAD);
				fd.setVisible(true);
				if (fd.getDirectory() != null) {
					rMgr = new ResourceManager();
					sicSim = new SicSimulator();
					File object_code_file = new File(fd.getDirectory() + fd.getFile());
					AllStepBtn.setEnabled(true);
					oneStepBtn.setEnabled(true);

					initialize(object_code_file, rMgr);

				}
			}
		});

		panel.add(FileOpenButton);

		JPanel panel_1 = new JPanel();
		panel_1.setBounds(10, 39, 528, 501);
		getContentPane().add(panel_1);
		panel_1.setLayout(new GridLayout(1, 2, 0, 0));

		JPanel panel_3 = new JPanel();
		panel_1.add(panel_3);
		panel_3.setLayout(null);

		JPanel panel_4 = new JPanel();
		panel_4.setBounds(0, 0, 264, 159);
		panel_4.setBorder(new TitledBorder(null, "H (Header Record)", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_3.add(panel_4);
		panel_4.setLayout(new GridLayout(3, 1, 0, 0));

		JPanel panel_7 = new JPanel();
		panel_4.add(panel_7);
		panel_7.setLayout(null);

		JLabel lblNewLabel_1 = new JLabel("Program Name: ");
		lblNewLabel_1.setBounds(33, 11, 94, 15);
		panel_7.add(lblNewLabel_1);

		HprogramName = new JTextField();
		HprogramName.setBounds(149, 8, 72, 21);
		HprogramName.setFont(new Font("Gulim", Font.PLAIN, 12));
		panel_7.add(HprogramName);
		HprogramName.setColumns(6);

		JPanel panel_8 = new JPanel();
		panel_4.add(panel_8);
		panel_8.setLayout(null);

		JLabel lblNewLabel_2 = new JLabel("Start Address of");
		lblNewLabel_2.setBounds(37, 10, 101, 15);
		panel_8.add(lblNewLabel_2);

		HprogramAdr = new JTextField();
		HprogramAdr.setBounds(150, 10, 72, 21);
		HprogramAdr.setFont(new Font("Gulim", Font.PLAIN, 12));
		panel_8.add(HprogramAdr);
		HprogramAdr.setColumns(6);

		JLabel lblNewLabel_10 = new JLabel("Object Program: ");
		lblNewLabel_10.setBounds(36, 25, 102, 15);
		panel_8.add(lblNewLabel_10);

		JPanel panel_9 = new JPanel();
		panel_4.add(panel_9);
		panel_9.setLayout(null);

		JLabel lblNewLabel_3 = new JLabel("Length of Program: ", JLabel.CENTER);
		lblNewLabel_3.setBounds(31, 8, 112, 15);
		panel_9.add(lblNewLabel_3);

		HprogramLen = new JTextField();
		HprogramLen.setBounds(151, 5, 72, 21);
		HprogramLen.setFont(new Font("Gulim", Font.PLAIN, 12));
		panel_9.add(HprogramLen);
		HprogramLen.setColumns(6);

		JPanel panel_5 = new JPanel();
		panel_5.setBounds(0, 167, 264, 177);
		panel_5.setBorder(new TitledBorder(null, "Register", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_3.add(panel_5);
		panel_5.setLayout(new GridLayout(6, 2, 0, 0));

		JPanel panel_14 = new JPanel();
		panel_5.add(panel_14);

		JPanel panel_12 = new JPanel();
		panel_5.add(panel_12);
		panel_12.setLayout(new GridLayout(0, 2, 0, 0));

		JLabel lblNewLabel_4 = new JLabel("Dec", JLabel.CENTER);
		panel_12.add(lblNewLabel_4);

		JLabel lblNewLabel_5 = new JLabel("Hex", JLabel.CENTER);
		panel_12.add(lblNewLabel_5);

		JLabel lblNewLabel_6 = new JLabel("A(#0)", JLabel.CENTER);
		panel_5.add(lblNewLabel_6);

		JPanel panel_15 = new JPanel();
		panel_5.add(panel_15);
		panel_15.setLayout(null);

		DecRegA = new JTextField();
		DecRegA.setBounds(6, 5, 54, 17);
		DecRegA.setHorizontalAlignment(SwingConstants.CENTER);
		DecRegA.setFont(new Font("Gulim", Font.PLAIN, 12));
		panel_15.add(DecRegA);
		DecRegA.setColumns(6);

		HexRegA = new JTextField();
		HexRegA.setBounds(65, 5, 54, 17);
		HexRegA.setHorizontalAlignment(SwingConstants.CENTER);
		HexRegA.setFont(new Font("Gulim", Font.PLAIN, 12));
		panel_15.add(HexRegA);
		HexRegA.setColumns(6);

		JLabel lblNewLabel_7 = new JLabel("X(#1)", JLabel.CENTER);
		panel_5.add(lblNewLabel_7);

		JPanel panel_16 = new JPanel();
		panel_5.add(panel_16);
		panel_16.setLayout(null);

		DecRegX = new JTextField();
		DecRegX.setBounds(6, 5, 54, 17);
		DecRegX.setHorizontalAlignment(SwingConstants.CENTER);
		DecRegX.setFont(new Font("Gulim", Font.PLAIN, 12));
		DecRegX.setColumns(6);
		panel_16.add(DecRegX);

		HexRegX = new JTextField();
		HexRegX.setBounds(65, 5, 54, 17);
		HexRegX.setHorizontalAlignment(SwingConstants.CENTER);
		HexRegX.setFont(new Font("Gulim", Font.PLAIN, 12));
		HexRegX.setColumns(6);
		panel_16.add(HexRegX);

		JLabel lblNewLabel_8 = new JLabel("L(#2)", JLabel.CENTER);
		panel_5.add(lblNewLabel_8);

		JPanel panel_17 = new JPanel();
		panel_5.add(panel_17);
		panel_17.setLayout(null);

		DecRegL = new JTextField();
		DecRegL.setBounds(6, 5, 54, 17);
		DecRegL.setHorizontalAlignment(SwingConstants.CENTER);
		DecRegL.setFont(new Font("Gulim", Font.PLAIN, 12));
		DecRegL.setColumns(6);
		panel_17.add(DecRegL);

		HexRegL = new JTextField();
		HexRegL.setBounds(65, 5, 54, 17);
		HexRegL.setHorizontalAlignment(SwingConstants.CENTER);
		HexRegL.setFont(new Font("Gulim", Font.PLAIN, 12));
		HexRegL.setColumns(6);
		panel_17.add(HexRegL);

		JLabel lblNewLabel_9 = new JLabel("PC(#8)", JLabel.CENTER);
		panel_5.add(lblNewLabel_9);

		JPanel panel_18 = new JPanel();
		panel_5.add(panel_18);
		panel_18.setLayout(null);

		DecRegPC = new JTextField();
		DecRegPC.setBounds(6, 5, 54, 17);
		DecRegPC.setHorizontalAlignment(SwingConstants.CENTER);
		DecRegPC.setFont(new Font("Gulim", Font.PLAIN, 12));
		DecRegPC.setColumns(6);
		panel_18.add(DecRegPC);

		HexRegPC = new JTextField();
		HexRegPC.setBounds(65, 5, 54, 17);
		HexRegPC.setHorizontalAlignment(SwingConstants.CENTER);
		HexRegPC.setFont(new Font("Gulim", Font.PLAIN, 12));
		HexRegPC.setColumns(6);
		panel_18.add(HexRegPC);

		JLabel lblSw = new JLabel("SW(#9)", JLabel.CENTER);
		panel_5.add(lblSw);

		JPanel panel_13 = new JPanel();
		panel_5.add(panel_13);
		panel_13.setLayout(null);

		RegSW = new JTextField();
		RegSW.setBounds(36, 5, 54, 17);
		RegSW.setHorizontalAlignment(SwingConstants.CENTER);
		RegSW.setFont(new Font("Gulim", Font.PLAIN, 12));
		panel_13.add(RegSW);
		RegSW.setColumns(6);

		JPanel panel_6 = new JPanel();
		panel_6.setBounds(0, 342, 264, 159);
		panel_6.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Register(for XE)", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel_3.add(panel_6);
		panel_6.setLayout(new GridLayout(5, 2, 0, 0));

		JPanel panel_19 = new JPanel();
		panel_6.add(panel_19);

		JPanel panel_20 = new JPanel();
		panel_6.add(panel_20);
		panel_20.setLayout(new GridLayout(0, 2, 0, 0));

		JLabel label = new JLabel("Dec");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		panel_20.add(label);

		JLabel label_1 = new JLabel("Hex");
		label_1.setHorizontalAlignment(SwingConstants.CENTER);
		panel_20.add(label_1);

		JLabel lblB = new JLabel("B(#3)", JLabel.CENTER);
		panel_6.add(lblB);

		JPanel panel_21 = new JPanel();
		panel_6.add(panel_21);
		panel_21.setLayout(null);

		DecRegB = new JTextField();
		DecRegB.setBounds(6, 5, 54, 17);
		DecRegB.setHorizontalAlignment(SwingConstants.CENTER);
		DecRegB.setFont(new Font("Gulim", Font.PLAIN, 12));
		DecRegB.setColumns(6);
		panel_21.add(DecRegB);

		HexRegB = new JTextField();
		HexRegB.setBounds(65, 5, 54, 17);
		HexRegB.setHorizontalAlignment(SwingConstants.CENTER);
		HexRegB.setFont(new Font("Gulim", Font.PLAIN, 12));
		HexRegB.setColumns(6);
		panel_21.add(HexRegB);

		JLabel lblS = new JLabel("S(#4)", JLabel.CENTER);
		panel_6.add(lblS);

		JPanel panel_22 = new JPanel();
		panel_6.add(panel_22);
		panel_22.setLayout(null);

		DecRegS = new JTextField();
		DecRegS.setBounds(6, 5, 54, 17);
		DecRegS.setHorizontalAlignment(SwingConstants.CENTER);
		DecRegS.setFont(new Font("Gulim", Font.PLAIN, 12));
		DecRegS.setColumns(6);
		panel_22.add(DecRegS);

		HexRegS = new JTextField();
		HexRegS.setBounds(65, 5, 54, 17);
		HexRegS.setHorizontalAlignment(SwingConstants.CENTER);
		HexRegS.setFont(new Font("Gulim", Font.PLAIN, 12));
		HexRegS.setColumns(6);
		panel_22.add(HexRegS);

		JLabel lblT = new JLabel("T(#5)", JLabel.CENTER);
		panel_6.add(lblT);

		JPanel panel_23 = new JPanel();
		panel_6.add(panel_23);
		panel_23.setLayout(null);

		DecRegT = new JTextField();
		DecRegT.setBounds(6, 5, 54, 17);
		DecRegT.setHorizontalAlignment(SwingConstants.CENTER);
		DecRegT.setFont(new Font("Gulim", Font.PLAIN, 12));
		DecRegT.setColumns(6);
		panel_23.add(DecRegT);

		HexRegT = new JTextField();
		HexRegT.setBounds(65, 5, 54, 17);
		HexRegT.setHorizontalAlignment(SwingConstants.CENTER);
		HexRegT.setFont(new Font("Gulim", Font.PLAIN, 12));
		HexRegT.setColumns(6);
		panel_23.add(HexRegT);

		JLabel lblF = new JLabel("F(#6)", JLabel.CENTER);
		panel_6.add(lblF);

		JPanel panel_25 = new JPanel();
		panel_6.add(panel_25);
		panel_25.setLayout(null);

		RegF = new JTextField();
		RegF.setBounds(12, 5, 102, 17);
		RegF.setHorizontalAlignment(SwingConstants.CENTER);
		RegF.setFont(new Font("Gulim", Font.PLAIN, 12));
		RegF.setColumns(12);
		panel_25.add(RegF);

		JPanel panel_24 = new JPanel();
		panel_1.add(panel_24);
		panel_24.setLayout(null);

		JPanel panel_26 = new JPanel();
		panel_26.setBorder(new TitledBorder(null, "E (End Record)", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_26.setBounds(10, 10, 242, 68);
		panel_24.add(panel_26);
		panel_26.setLayout(null);

		JLabel lblAddressOfFirst = new JLabel("Address of First Instruction");
		lblAddressOfFirst.setBounds(44, 20, 170, 15);
		panel_26.add(lblAddressOfFirst);

		JLabel lblInObjectProgram = new JLabel("in Object Program :");
		lblInObjectProgram.setBounds(24, 45, 110, 15);
		panel_26.add(lblInObjectProgram);

		EFirstInstAdr = new JTextField();
		EFirstInstAdr.setBounds(142, 42, 72, 21);
		EFirstInstAdr.setFont(new Font("Gulim", Font.PLAIN, 12));
		panel_26.add(EFirstInstAdr);
		EFirstInstAdr.setColumns(6);

		JPanel panel_27 = new JPanel();
		panel_27.setBounds(10, 78, 254, 413);
		panel_24.add(panel_27);
		panel_27.setLayout(null);

		JPanel panel_28 = new JPanel();
		panel_28.setBounds(0, 0, 242, 86);
		panel_27.add(panel_28);
		panel_28.setLayout(null);

		JLabel lblNewLabel_12 = new JLabel(" Start Address in Memory :");
		lblNewLabel_12.setBounds(0, 23, 161, 15);
		panel_28.add(lblNewLabel_12);

		JLabel lblTargetAddress = new JLabel("Target Address :");
		lblTargetAddress.setBounds(25, 51, 101, 25);
		panel_28.add(lblTargetAddress);

		TarAdr = new JTextField();
		TarAdr.setBounds(160, 53, 80, 21);
		panel_28.add(TarAdr);
		TarAdr.setColumns(6);

		StAdr = new JTextField();
		StAdr.setBounds(160, 20, 80, 21);
		panel_28.add(StAdr);
		StAdr.setColumns(6);

		JPanel panel_30 = new JPanel();
		panel_30.setBounds(0, 85, 254, 307);
		panel_27.add(panel_30);
		panel_30.setLayout(new GridLayout(1, 2, 0, 0));

		JPanel panel_31 = new JPanel();
		panel_30.add(panel_31);
		panel_31.setLayout(new BorderLayout(0, 10));

		JLabel lblInstructions = new JLabel(" Instructions :");
		panel_31.add(lblInstructions, BorderLayout.NORTH);

		JPanel panel_33 = new JPanel();
		panel_33.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel_31.add(panel_33, BorderLayout.CENTER);
		panel_33.setLayout(new BorderLayout(0, 0));
		InstText = new JTextArea();
		JScrollPane scrollPane2 = new JScrollPane(InstText);
		scrollPane2.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		panel_33.add(scrollPane2);

		JPanel panel_32 = new JPanel();
		panel_30.add(panel_32);
		panel_32.setLayout(null);

		JLabel lblNewLabel_11 = new JLabel("Using Device");
		lblNewLabel_11.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_11.setFont(new Font("Gulim", Font.PLAIN, 12));
		lblNewLabel_11.setBounds(12, 53, 97, 32);
		panel_32.add(lblNewLabel_11);

		UsingDev = new JTextField();
		UsingDev.setBounds(12, 95, 97, 21);
		panel_32.add(UsingDev);
		UsingDev.setColumns(8);

		oneStepBtn.setFont(new Font("Gulim", Font.PLAIN, 11));
		oneStepBtn.setBounds(12, 187, 97, 23);
		panel_32.add(oneStepBtn);

		AllStepBtn.setBounds(12, 230, 97, 21);
		panel_32.add(AllStepBtn);

		QuitBtn.setBounds(12, 274, 99, 23);
		panel_32.add(QuitBtn);

		JPanel panel_2 = new JPanel();
		panel_2.setBounds(12, 550, 526, 111);
		getContentPane().add(panel_2);
		panel_2.setLayout(new BorderLayout(0, 7));

		JLabel lblNewLabel = new JLabel("Log (For excute to instruction): ");
		panel_2.add(lblNewLabel, BorderLayout.NORTH);

		JPanel panel_29 = new JPanel();
		panel_29.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel_2.add(panel_29, BorderLayout.CENTER);

		panel_29.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane(LogText);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		panel_29.add(scrollPane, BorderLayout.CENTER);

		this.setVisible(true);

		this.setSize(570, 730);
		this.setTitle("");// 타이틀 설정
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	// 시뮬레이터를 동작시키기 위한 세팅을 수행한다.
	// sic 시뮬레이터를 통해 로더를 수행시키고, 로드된 값들을 읽어 보여주어
	// 스텝을 진행할 수 있는 상태로 만들어 놓는다.
	public void initialize(File objFile, ResourceManager rMgr) {

		sicSim.initialize(objFile, rMgr);

		FileDialog fd = new FileDialog(new Frame(), "Copy Source ", FileDialog.LOAD);
		fd.setVisible(true);
		if (fd.getDirectory() != null) {
			rMgr.initialDevice(fd.getDirectory() + fd.getFile());
		}
		InputFileName.setText(objFile.getName());
		HprogramName.setText(rMgr.getProgName(0));
		HprogramAdr.setText(String.format("%06X", rMgr.getStartAdr(0)));
		HprogramLen.setText(String.format("%06X", rMgr.getProgLength(0) + rMgr.getProgLength(1) + rMgr.getProgLength(2)));
		EFirstInstAdr.setText(String.format("%06X", rMgr.getStartAdr(0)));
		StAdr.setText(String.format("%06X", rMgr.getStartAdr(0)));

		UsingDev.setText(fd.getFile());
		update();

	}

	// 하나의 명령어만 수행하는 메소드로써 sic 시뮬레이터에게 작업을 전달한다.
	public void oneStep() {
		sicSim.oneStep();
		update();
		LogText.setText(LogText.getText() + sicSim.logText);//로그를 보여주는 텍스트 창
		InstText.setText(InstText.getText() + sicSim.InstText);//명령어를 보여주는 텍스트창
	}

	// 남은 명령어를 모두 수행하는 메소드로써 sic 시뮬레이터에 작업을 전달
	public void allStep() {
		boolean set = true;
		String logtemp = LogText.getText();// 현제 쓰여있는 값을 가져옴 
		String insTemp = InstText.getText();//현제 쓰여있는 값을 가져옴
		update();
		while (set) {
			set = sicSim.oneStep();
			logtemp += sicSim.logText;
			insTemp += sicSim.InstText;
		}
		update();//끝나면 값들을 띄어줌 
		LogText.setText(logtemp);//실행 결과에 대한 로그를 띄어줌  
		InstText.setText(insTemp);//실행 결과에대한 명령어들을 띄어쥼 
		new JOptionPane().showMessageDialog(null, "Object Code execution end.");
	}

	// 작업이 완료되었을 때 변화된 결과를 화면에 업데이트
	public void update() {

		DecRegA.setText(String.format("%06d", rMgr.getRegister(0)));
		HexRegA.setText(String.format("%06X", rMgr.getRegister(0)));

		DecRegX.setText(String.format("%06d", rMgr.getRegister(1)));
		HexRegX.setText(String.format("%06X", rMgr.getRegister(1)));

		DecRegL.setText(String.format("%06d", rMgr.getRegister(2)));
		HexRegL.setText(String.format("%06X", rMgr.getRegister(2)));

		DecRegB.setText(String.format("%06d", rMgr.getRegister(3)));
		HexRegB.setText(String.format("%06X", rMgr.getRegister(3)));

		DecRegS.setText(String.format("%06d", rMgr.getRegister(4)));
		HexRegS.setText(String.format("%06X", rMgr.getRegister(4)));

		DecRegT.setText(String.format("%06d", rMgr.getRegister(5)));
		HexRegT.setText(String.format("%06X", rMgr.getRegister(5)));

		RegF.setText(String.format("%012X", rMgr.getRegister(6)));

		TarAdr.setText(String.format("%06X", rMgr.getRegister(7)));

		DecRegPC.setText(String.format("%06d", rMgr.getRegister(8)));
		HexRegPC.setText(String.format("%06X", rMgr.getRegister(8)));

		RegSW.setText(String.format("%06X", rMgr.getRegister(9)));

		if ((rMgr.getStartAdr(0) == rMgr.getRegister(8)) && (rMgr.getRegister(8) < rMgr.getStartAdr(1))) {//pc값이 섹터 0의 사이라면 
			HprogramAdr.setText(String.format("%06X", rMgr.getStartAdr(0)));//섹터0의 시작 주소
			HprogramLen.setText(String.format("%06X", rMgr.getProgLength(0)));//섹터0의 길이
			HprogramName.setText(rMgr.getProgName(0));//섹터 0의 이름 
		} else if ((rMgr.getStartAdr(1) < rMgr.getRegister(8)) && (rMgr.getRegister(8) < rMgr.getStartAdr(2))) {//pc값이 섹터 1의 사이라면 
			HprogramAdr.setText(String.format("%06X", rMgr.getStartAdr(1)));//섹터1의 시작 주소
			HprogramLen.setText(String.format("%06X", rMgr.getProgLength(1)));//섹터1의 길이
			HprogramName.setText(rMgr.getProgName(1));//섹터 1의 이름 
		}
		else if ((rMgr.getStartAdr(2) < rMgr.getRegister(8))) {//pc값이 섹테 2의 사이라면 
			HprogramAdr.setText(String.format("%06X", rMgr.getStartAdr(2)));//섹터2의 시작 주소
			HprogramLen.setText(String.format("%06X", rMgr.getProgLength(2)));//섹터2의 길이
			HprogramName.setText(rMgr.getProgName(2));//섹터 2의 이름 
		}

		//각각의 레지스터의 값을 창에 띄우기 위해 업데이트 해준다. 

	}
}
