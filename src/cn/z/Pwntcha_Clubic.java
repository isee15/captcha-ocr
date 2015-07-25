package cn.z;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import cn.z.util.FontGlyphs;
import cn.z.util.ImageUtil;

public class Pwntcha_Clubic {

	public static void main(String[] args) throws Exception {

		final String fontFile = "img/clubic/font.png";
		final HashMap<BufferedImage, FontGlyphs> fontMap = ImageUtil.loadFontVariable(fontFile, "0123456789");
		for (int k = 0; k < 100; k++) {
			final String picFile = String.format("img/clubic/clubic_%03d.png", k);
			BufferedImage img = ImageIO.read(new File(picFile));
			img = ImageUtil.filterThreshold(img, 200);
			final StringBuilder result = new StringBuilder();
			int x, y;
			int g, r;
			int xmin, xmax, ymin, ymax, startx = 0, cur = 0;
			int distmin, distx = 0;
			BufferedImage distch = null;
			final int w = img.getWidth();
			final int h = img.getHeight();
			for (y = 0; y < h; y++) {
				for (x = 0; x < w; x++) {
					g = new Color(img.getRGB(x, y)).getGreen();
					img.setRGB(x, y, new Color(255, g, 255).getRGB());
				}
			}
			while (cur < 6) {
				distmin = Integer.MAX_VALUE;
				for (final Entry<BufferedImage, FontGlyphs> entry : fontMap.entrySet()) {
					int localmin = Integer.MAX_VALUE;
					int localx = 0;
					final BufferedImage fontImg = entry.getKey();
					xmin = 0;
					ymin = 0;
					xmax = fontImg.getWidth();
					ymax = fontImg.getHeight();
					for (y = -4; y < 4; y++) {
						for (x = startx; x < startx + 4; x++) {
							int z, t, dist;
							dist = 0;
							for (t = 0; t < ymax - ymin; t++) {
								for (z = 0; z < xmax - xmin; z++) {
									int r2;
									r = new Color(fontImg.getRGB(xmin + z, ymin + t)).getGreen();
									if (x + z >= w || y + t >= h || x + z < 0 || y + t < 0) {
										r2 = 255;
									} else {
										r2 = new Color(img.getRGB(x + z, y + t)).getGreen();
									}

									dist += Math.abs(r - r2);
								}
							}
							dist = dist * 128 / entry.getValue().weight;
							if (dist < localmin) {
								localmin = dist;
								localx = x;
							}
						}
					}
					if (localmin < distmin) {
						distmin = localmin;
						distx = localx;
						distch = entry.getKey();
					}
				}
				cur++;
				startx = distx + distch.getWidth();
				result.append(fontMap.get(distch).c);
			}
			System.out.println(result.toString());
		}
	}

}
