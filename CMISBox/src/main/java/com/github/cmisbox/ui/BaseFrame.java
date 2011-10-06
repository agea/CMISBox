package com.github.cmisbox.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Paint;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.Semaphore;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class BaseFrame extends javax.swing.JFrame implements
		javax.swing.RootPaneContainer {

	static final class CloseAdapter extends MouseAdapter {

		private BaseFrame f;

		public CloseAdapter(BaseFrame baseFrame) {
			this.f = baseFrame;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			this.f.setVisible(false);
			this.f.dispose();
			this.f.semaphore.release();
		}
	}

	private static final long serialVersionUID = 4679039774438171041L;

	private final boolean gradient;

	protected JPanel mainPanel;

	protected Semaphore semaphore = new Semaphore(0);

	protected Log log;

	protected CloseAdapter closeAdapter = new CloseAdapter(this);

	private static final GraphicsConfiguration gc = GraphicsEnvironment
			.getLocalGraphicsEnvironment().getDefaultScreenDevice()
			.getDefaultConfiguration();

	public BaseFrame() {
		super(
				AWTUtilitiesWrapper.isTranslucencyCapable(BaseFrame.gc) ? BaseFrame.gc
						: null);
		this.log = LogFactory.getLog(this.getClass());

		this.gradient = false;
		this.setUndecorated(true);
		this.mainPanel = new JPanel(new GridBagLayout()) {

			private static final long serialVersionUID = 1035974033526970010L;

			protected void paintComponent(Graphics g) {
				if ((g instanceof Graphics2D) && BaseFrame.this.gradient) {
					final int R = 0;
					final int G = 0;
					final int B = 0;

					Paint p = new GradientPaint(0.0f, 0.0f, new Color(R, G, B,
							192), this.getWidth(), this.getHeight(), new Color(
							R, G, B, 255), true);
					Graphics2D g2d = (Graphics2D) g;
					g2d.setPaint(p);
					g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
				} else {
					super.paintComponent(g);
				}
			}
		};

		this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

		GridBagConstraints gbc = new GridBagConstraints();

		this.mainPanel.setDoubleBuffered(false);
		this.mainPanel.setOpaque(false);
		this.mainPanel.setBorder(BorderFactory.createLineBorder(Color.white));

		JLabel title = new JLabel(this.getWindowTitle(), SwingConstants.CENTER);
		title.setForeground(Color.white);

		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 100;

		this.mainPanel.add(title, gbc);

		Image closeImg = this.getImage("images/application-exit.png", 32, 32);
		JLabel close = new JLabel(new ImageIcon(closeImg), SwingConstants.RIGHT);

		gbc.fill = GridBagConstraints.NONE;
		gbc.gridx = 2;
		gbc.weightx = 0;

		this.mainPanel.add(close, gbc);

		close.addMouseListener(this.closeAdapter);

		this.getContentPane().add(this.mainPanel, BorderLayout.CENTER);

		this.initComponents();

		this.pack();

		this.mainPanel.setOpaque(!this.gradient);
		if (!this.gradient) {
			this.mainPanel.setBackground(new Color(0, 0, 0, 208));
		}

		this.setLocationRelativeTo(null);
		AWTUtilitiesWrapper.setWindowOpaque(this, false);
		this.setVisible(true);
		this.setAlwaysOnTop(true);

	}

	public Image getImage(String resource, Integer height, Integer width) {
		try {

			BufferedImage image = ImageIO.read(this.getClass().getResource(
					resource));

			if ((height != null)
					&& (width != null)
					&& ((image.getHeight() != height) || (image.getWidth() != width))) {
				return image.getScaledInstance(width, height,
						Image.SCALE_SMOOTH);
			}

			return image;
		} catch (IOException e1) {
			this.log.error(e1);
		}
		return null;
	}

	protected abstract String getWindowTitle();

	protected abstract void initComponents();

	public void waitFor() {
		try {
			this.semaphore.acquire();
		} catch (InterruptedException e) {
			this.log.error(e);
		}
	}

}
