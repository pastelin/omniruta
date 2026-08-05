package com.HealthAdvanced.healthAdvanced.HEADPrescription.renderer;

import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.response.HEADSignaturePointDto;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.response.HEADSignatureVectorDto;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.List;

public final class HEADSignatureRenderer {

    private HEADSignatureRenderer() {}

    public static BufferedImage renderToImage(HEADSignatureVectorDto v, int outW, int outH) {
        var img = new BufferedImage(outW, outH, BufferedImage.TYPE_INT_ARGB);
        var g = img.createGraphics();

        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            g.setComposite(AlphaComposite.Src);
            g.setColor(new Color(255, 255, 255, 0));
            g.fillRect(0, 0, outW, outH);

            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            if (v == null || v.strokes() == null) return img;

            for (var stroke : v.strokes()) {
                if (stroke == null || stroke.points() == null) continue;

                var pts = stroke.points();
                if (pts.isEmpty()) continue;

                if (pts.size() == 1) {
                    drawDot(g, pts.get(0), outW, outH);
                    continue;
                }

                // Path suave por midpoints (sin for por index)
                Path2D.Float path = new Path2D.Float();

                var it = pts.iterator();
                HEADSignaturePointDto p0 = it.next();
                float x0 = p0.x() * outW;
                float y0 = p0.y() * outH;
                path.moveTo(x0, y0);

                HEADSignaturePointDto prev = p0;
                while (it.hasNext()) {
                    HEADSignaturePointDto cur = it.next();

                    float x1 = cur.x() * outW;
                    float y1 = cur.y() * outH;

                    float midX = (prev.x() * outW + x1) / 2f;
                    float midY = (prev.y() * outH + y1) / 2f;

                    // quadratic curve hacia el midpoint
                    path.quadTo(prev.x() * outW, prev.y() * outH, midX, midY);

                    prev = cur;
                }

                // termina en el último punto
                path.lineTo(prev.x() * outW, prev.y() * outH);

                g.draw(path);
            }

            return img;
        } finally {
            g.dispose();
        }
    }

    private static void drawDot(Graphics2D g, HEADSignaturePointDto p, int outW, int outH) {
        int x = Math.round(p.x() * outW);
        int y = Math.round(p.y() * outH);
        g.fillOval(x - 3, y - 3, 6, 6);
    }
}
