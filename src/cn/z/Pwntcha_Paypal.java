package cn.z;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import cn.z.util.FontGlyphs;
import cn.z.util.ImageUtil;

public class Pwntcha_Paypal {

	public static void main(String[] args) throws Exception {

		String fontFile = "img/paypal/stencil_23_AZ.bmp";
		final HashMap<BufferedImage, FontGlyphs> fontMap = ImageUtil.loadFontVariable(fontFile,
				"0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		fontFile = "img/paypal/stencil_24_AZ.bmp";
		final HashMap<BufferedImage, FontGlyphs> fontMap2 = ImageUtil.loadFontVariable(fontFile,
				"0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		fontMap.putAll(fontMap2);
		for (int k = 0; k < 100; k++) {
			final StringBuilder result = new StringBuilder();
			final String picFile = String.format("img/paypal/paypal_%03d.jpeg", k);
			final BufferedImage img = ImageIO.read(new File(picFile));
			int x, y;
			int r;
			int xmin, xmax, ymin, ymax, startx = 0, cur = 0;
			int bestdist, bestx = 0;

			final int w = img.getWidth();
			final int h = img.getHeight();
			final int DELTA = 2;
			BufferedImage bestch = null;
			while (cur < 8) {

				bestdist = Integer.MAX_VALUE;
				for (final Entry<BufferedImage, FontGlyphs> entry : fontMap.entrySet()) {

					int localmin = Integer.MAX_VALUE;
					int localx = 0;
					final BufferedImage fontImg = entry.getKey();
					xmin = -DELTA;
					ymin = 0;
					xmax = fontImg.getWidth() + DELTA;
					ymax = fontImg.getHeight();
					for (y = -3; y < 1; y++) {
						for (x = startx; x < startx + 15; x++) {
							int z, t, dist;
							dist = 0;
							for (t = 0; t < ymax - ymin; t++) {
								for (z = 0; z < xmax - xmin; z++) {
									int r2;
									if (xmin + z >= fontImg.getWidth() || ymin + t >= fontImg.getHeight()
											|| xmin + z < 0 || ymin + t < 0) {
										r = 255;
									} else {
										r = new Color(fontImg.getRGB(xmin + z, ymin + t)).getGreen();
									}

									if (x + z >= w || y + t >= h || x + z < 0 || y + t < 0) {
										r2 = 255;
									} else {
										r2 = new Color(img.getRGB(x + z, y + t)).getGreen();
									}
									if (r < r2) {
										dist += (r - r2) * (r - r2);
									} else {
										dist += (r - r2) * (r - r2) / 2;
									}
								}
							}
							dist = dist / (xmax - xmin - 2 * DELTA) / (xmax - xmin - 2 * DELTA);
							if (dist < localmin) {
								localmin = dist;
								localx = x;
							}
						}
					}
					if (localmin < bestdist) {
						bestdist = localmin;
						bestx = localx;
						bestch = entry.getKey();
					}
				}
				cur++;
				startx = bestx + bestch.getWidth();
				result.append(fontMap.get(bestch).c);
			}
			System.out.println(result.toString());
		}
	}

}
