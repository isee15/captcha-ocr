package cn.z.svm;

import java.applet.Applet;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

public class svm_toy extends Applet {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	static final String DEFAULT_PARAM = "-t 2 -c 100";
	int XLEN;
	int YLEN;

	// off-screen buffer

	Image buffer;
	Graphics buffer_gc;

	// pre-allocated colors

	final static Color colors[] = { new Color(0, 0, 0), new Color(0, 120, 120), new Color(120, 120, 0),
			new Color(120, 0, 120), new Color(0, 200, 200), new Color(200, 200, 0), new Color(200, 0, 200) };

	class point {
		point(double x, double y, byte value) {
			this.x = x;
			this.y = y;
			this.value = value;
		}

		double x, y;
		byte value;
	}

	Vector<point> point_list = new Vector<point>();
	byte current_value = 1;

	@Override
	public void init() {
		this.setSize(this.getSize());

		final Button button_change = new Button("Change");
		final Button button_run = new Button("Run");
		final Button button_clear = new Button("Clear");
		final Button button_save = new Button("Save");
		final Button button_load = new Button("Load");
		final TextField input_line = new TextField(DEFAULT_PARAM);

		final BorderLayout layout = new BorderLayout();
		this.setLayout(layout);

		final Panel p = new Panel();
		final GridBagLayout gridbag = new GridBagLayout();
		p.setLayout(gridbag);

		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridwidth = 1;
		gridbag.setConstraints(button_change, c);
		gridbag.setConstraints(button_run, c);
		gridbag.setConstraints(button_clear, c);
		gridbag.setConstraints(button_save, c);
		gridbag.setConstraints(button_load, c);
		c.weightx = 5;
		c.gridwidth = 5;
		gridbag.setConstraints(input_line, c);

		button_change.setBackground(colors[this.current_value]);

		p.add(button_change);
		p.add(button_run);
		p.add(button_clear);
		p.add(button_save);
		p.add(button_load);
		p.add(input_line);
		this.add(p, BorderLayout.SOUTH);

		button_change.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				svm_toy.this.button_change_clicked();
				button_change.setBackground(colors[svm_toy.this.current_value]);
			}
		});

		button_run.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				svm_toy.this.button_run_clicked(input_line.getText());
			}
		});

		button_clear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				svm_toy.this.button_clear_clicked();
			}
		});

		button_save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				svm_toy.this.button_save_clicked();
			}
		});

		button_load.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				svm_toy.this.button_load_clicked();
			}
		});

		input_line.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				svm_toy.this.button_run_clicked(input_line.getText());
			}
		});

		this.enableEvents(AWTEvent.MOUSE_EVENT_MASK);
	}

	void draw_point(point p) {
		final Color c = colors[p.value + 3];

		final Graphics window_gc = this.getGraphics();
		this.buffer_gc.setColor(c);
		this.buffer_gc.fillRect((int) (p.x * this.XLEN), (int) (p.y * this.YLEN), 4, 4);
		window_gc.setColor(c);
		window_gc.fillRect((int) (p.x * this.XLEN), (int) (p.y * this.YLEN), 4, 4);
	}

	void clear_all() {
		this.point_list.removeAllElements();
		if (this.buffer != null) {
			this.buffer_gc.setColor(colors[0]);
			this.buffer_gc.fillRect(0, 0, this.XLEN, this.YLEN);
		}
		this.repaint();
	}

	void draw_all_points() {
		final int n = this.point_list.size();
		for (int i = 0; i < n; i++) {
			this.draw_point(this.point_list.elementAt(i));
		}
	}

	void button_change_clicked() {
		++this.current_value;
		if (this.current_value > 3) {
			this.current_value = 1;
		}
	}

	private static double atof(String s) {
		return Double.valueOf(s).doubleValue();
	}

	private static int atoi(String s) {
		return Integer.parseInt(s);
	}

	void button_run_clicked(String args) {
		// guard
		if (this.point_list.isEmpty()) {
			return;
		}

		final svm_parameter param = new svm_parameter();

		// default values
		param.svm_type = svm_parameter.C_SVC;
		param.kernel_type = svm_parameter.RBF;
		param.degree = 3;
		param.gamma = 0;
		param.coef0 = 0;
		param.nu = 0.5;
		param.cache_size = 40;
		param.C = 1;
		param.eps = 1e-3;
		param.p = 0.1;
		param.shrinking = 1;
		param.probability = 0;
		param.nr_weight = 0;
		param.weight_label = new int[0];
		param.weight = new double[0];

		// parse options
		final StringTokenizer st = new StringTokenizer(args);
		final String[] argv = new String[st.countTokens()];
		for (int i = 0; i < argv.length; i++) {
			argv[i] = st.nextToken();
		}

		for (int i = 0; i < argv.length; i++) {
			if (argv[i].charAt(0) != '-') {
				break;
			}
			if (++i >= argv.length) {
				System.err.print("unknown option\n");
				break;
			}
			switch (argv[i - 1].charAt(1)) {
			case 's':
				param.svm_type = atoi(argv[i]);
				break;
			case 't':
				param.kernel_type = atoi(argv[i]);
				break;
			case 'd':
				param.degree = atoi(argv[i]);
				break;
			case 'g':
				param.gamma = atof(argv[i]);
				break;
			case 'r':
				param.coef0 = atof(argv[i]);
				break;
			case 'n':
				param.nu = atof(argv[i]);
				break;
			case 'm':
				param.cache_size = atof(argv[i]);
				break;
			case 'c':
				param.C = atof(argv[i]);
				break;
			case 'e':
				param.eps = atof(argv[i]);
				break;
			case 'p':
				param.p = atof(argv[i]);
				break;
			case 'h':
				param.shrinking = atoi(argv[i]);
				break;
			case 'b':
				param.probability = atoi(argv[i]);
				break;
			case 'w':
				++param.nr_weight; {
				final int[] old = param.weight_label;
				param.weight_label = new int[param.nr_weight];
				System.arraycopy(old, 0, param.weight_label, 0, param.nr_weight - 1);
			}

			{
				final double[] old = param.weight;
				param.weight = new double[param.nr_weight];
				System.arraycopy(old, 0, param.weight, 0, param.nr_weight - 1);
			}

				param.weight_label[param.nr_weight - 1] = atoi(argv[i - 1].substring(2));
				param.weight[param.nr_weight - 1] = atof(argv[i]);
				break;
			default:
				System.err.print("unknown option\n");
			}
		}

		// build problem
		final svm_problem prob = new svm_problem();
		prob.l = this.point_list.size();
		prob.y = new double[prob.l];

		if (param.kernel_type == svm_parameter.PRECOMPUTED) {
		} else if (param.svm_type == svm_parameter.EPSILON_SVR || param.svm_type == svm_parameter.NU_SVR) {
			if (param.gamma == 0) {
				param.gamma = 1;
			}
			prob.x = new svm_node[prob.l][1];
			for (int i = 0; i < prob.l; i++) {
				final point p = this.point_list.elementAt(i);
				prob.x[i][0] = new svm_node();
				prob.x[i][0].index = 1;
				prob.x[i][0].value = p.x;
				prob.y[i] = p.y;
			}

			// build model & classify
			final svm_model model = svm.svm_train(prob, param);
			final svm_node[] x = new svm_node[1];
			x[0] = new svm_node();
			x[0].index = 1;
			final int[] j = new int[this.XLEN];

			final Graphics window_gc = this.getGraphics();
			for (int i = 0; i < this.XLEN; i++) {
				x[0].value = (double) i / this.XLEN;
				j[i] = (int) (this.YLEN * svm.svm_predict(model, x));
			}

			this.buffer_gc.setColor(colors[0]);
			this.buffer_gc.drawLine(0, 0, 0, this.YLEN - 1);
			window_gc.setColor(colors[0]);
			window_gc.drawLine(0, 0, 0, this.YLEN - 1);

			final int p = (int) (param.p * this.YLEN);
			for (int i = 1; i < this.XLEN; i++) {
				this.buffer_gc.setColor(colors[0]);
				this.buffer_gc.drawLine(i, 0, i, this.YLEN - 1);
				window_gc.setColor(colors[0]);
				window_gc.drawLine(i, 0, i, this.YLEN - 1);

				this.buffer_gc.setColor(colors[5]);
				window_gc.setColor(colors[5]);
				this.buffer_gc.drawLine(i - 1, j[i - 1], i, j[i]);
				window_gc.drawLine(i - 1, j[i - 1], i, j[i]);

				if (param.svm_type == svm_parameter.EPSILON_SVR) {
					this.buffer_gc.setColor(colors[2]);
					window_gc.setColor(colors[2]);
					this.buffer_gc.drawLine(i - 1, j[i - 1] + p, i, j[i] + p);
					window_gc.drawLine(i - 1, j[i - 1] + p, i, j[i] + p);

					this.buffer_gc.setColor(colors[2]);
					window_gc.setColor(colors[2]);
					this.buffer_gc.drawLine(i - 1, j[i - 1] - p, i, j[i] - p);
					window_gc.drawLine(i - 1, j[i - 1] - p, i, j[i] - p);
				}
			}
		} else {
			if (param.gamma == 0) {
				param.gamma = 0.5;
			}
			prob.x = new svm_node[prob.l][2];
			for (int i = 0; i < prob.l; i++) {
				final point p = this.point_list.elementAt(i);
				prob.x[i][0] = new svm_node();
				prob.x[i][0].index = 1;
				prob.x[i][0].value = p.x;
				prob.x[i][1] = new svm_node();
				prob.x[i][1].index = 2;
				prob.x[i][1].value = p.y;
				prob.y[i] = p.value;
			}

			// build model & classify
			final svm_model model = svm.svm_train(prob, param);
			final svm_node[] x = new svm_node[2];
			x[0] = new svm_node();
			x[1] = new svm_node();
			x[0].index = 1;
			x[1].index = 2;

			final Graphics window_gc = this.getGraphics();
			for (int i = 0; i < this.XLEN; i++) {
				for (int j = 0; j < this.YLEN; j++) {
					x[0].value = (double) i / this.XLEN;
					x[1].value = (double) j / this.YLEN;
					double d = svm.svm_predict(model, x);
					if (param.svm_type == svm_parameter.ONE_CLASS && d < 0) {
						d = 2;
					}
					this.buffer_gc.setColor(colors[(int) d]);
					window_gc.setColor(colors[(int) d]);
					this.buffer_gc.drawLine(i, j, i, j);
					window_gc.drawLine(i, j, i, j);
				}
			}
		}

		this.draw_all_points();
	}

	void button_clear_clicked() {
		this.clear_all();
	}

	void button_save_clicked() {
		final FileDialog dialog = new FileDialog(new Frame(), "Save", FileDialog.SAVE);
		dialog.setVisible(true);
		final String filename = dialog.getFile();
		if (filename == null) {
			return;
		}
		try {
			final DataOutputStream fp = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
			final int n = this.point_list.size();
			for (int i = 0; i < n; i++) {
				final point p = this.point_list.elementAt(i);
				fp.writeBytes(p.value + " 1:" + p.x + " 2:" + p.y + "\n");
			}
			fp.close();
		} catch (final IOException e) {
			System.err.print(e);
		}
	}

	void button_load_clicked() {
		final FileDialog dialog = new FileDialog(new Frame(), "Load", FileDialog.LOAD);
		dialog.setVisible(true);
		final String filename = dialog.getFile();
		if (filename == null) {
			return;
		}
		this.clear_all();
		try {
			final BufferedReader fp = new BufferedReader(new FileReader(filename));
			String line;
			while ((line = fp.readLine()) != null) {
				final StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");
				final byte value = (byte) atoi(st.nextToken());
				st.nextToken();
				final double x = atof(st.nextToken());
				st.nextToken();
				final double y = atof(st.nextToken());
				this.point_list.addElement(new point(x, y, value));
			}
			fp.close();
		} catch (final IOException e) {
			System.err.print(e);
		}
		this.draw_all_points();
	}

	@Override
	protected void processMouseEvent(MouseEvent e) {
		if (e.getID() == MouseEvent.MOUSE_PRESSED) {
			if (e.getX() >= this.XLEN || e.getY() >= this.YLEN) {
				return;
			}
			final point p = new point((double) e.getX() / this.XLEN, (double) e.getY() / this.YLEN, this.current_value);
			this.point_list.addElement(p);
			this.draw_point(p);
		}
	}

	@Override
	public void paint(Graphics g) {
		// create buffer first time
		if (this.buffer == null) {
			this.buffer = this.createImage(this.XLEN, this.YLEN);
			this.buffer_gc = this.buffer.getGraphics();
			this.buffer_gc.setColor(colors[0]);
			this.buffer_gc.fillRect(0, 0, this.XLEN, this.YLEN);
		}
		g.drawImage(this.buffer, 0, 0, this);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(this.XLEN, this.YLEN + 50);
	}

	@Override
	public void setSize(Dimension d) {
		this.setSize(d.width, d.height);
	}

	@Override
	public void setSize(int w, int h) {
		super.setSize(w, h);
		this.XLEN = w;
		this.YLEN = h - 50;
		this.clear_all();
	}

	public static void main(String[] argv) {
		new AppletFrame("svm_toy", new svm_toy(), 500, 500 + 50);
	}
}

class AppletFrame extends Frame {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	AppletFrame(String title, Applet applet, int width, int height) {
		super(title);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		applet.init();
		applet.setSize(width, height);
		applet.start();
		this.add(applet);
		this.pack();
		this.setVisible(true);
	}
}
