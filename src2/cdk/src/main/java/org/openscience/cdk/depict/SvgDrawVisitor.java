/*
 * Copyright (c) 2015 John May <jwmay@users.sf.net>
 *
 * Contact: cdk-devel@lists.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version. All we ask is that proper credit is given
 * for our work, which includes - but is not limited to - adding the above
 * copyright notice to the beginning of your source code files, and to any
 * copyright notice that you may distribute with programs based on this work.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 U
 */

package org.openscience.cdk.depict;

import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.elements.Bounds;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.GeneralPath;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.LineElement;
import org.openscience.cdk.renderer.elements.MarkedElement;
import org.openscience.cdk.renderer.elements.OvalElement;
import org.openscience.cdk.renderer.elements.RectangleElement;
import org.openscience.cdk.renderer.elements.TextElement;
import org.openscience.cdk.renderer.elements.path.PathElement;
import org.openscience.cdk.renderer.font.IFontManager;
import org.openscience.cdk.renderer.generators.standard.StandardGenerator;
import org.openscience.cdk.renderer.visitor.IDrawVisitor;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Internal - An SvgDrawVisitor, currently only certain elements are supported
 * but covers depictions generated by the {@link StandardGenerator}
 * (only {@link LineElement} and {@link GeneralPath}).
 * 
 * Usage:
 * <pre>{@link
 * SvgDrawVisitor visitor = new SvgDrawVisitor(50, 50)
 * visitor.visit(renderingElements);
 * String svg = visitor.toString();
 * }</pre>
 */
final class SvgDrawVisitor implements IDrawVisitor {

    private final StringBuilder sb = new StringBuilder(5000);

    private int             indentLvl     = 0;
    private AffineTransform transform     = null;
    private RendererModel   model         = null;
    private static final NumberFormat    decimalFormat = new DecimalFormat(".###",
                                                                           new DecimalFormatSymbols(Locale.ROOT));

    private boolean defaultsWritten    = false;
    private Color   defaultStroke      = null;
    private Color   defaultFill        = null;
    private String  defaultStrokeWidth = null;

    private static double round(double d) {
        return Double.parseDouble(decimalFormat.format(d));
    }

    /**
     * Create an SvgDrawVisitor with the specified width/height
     *
     * @param w width of canvas in 'units'
     * @param h height of canvas in 'units'
     * @param units 'px' or 'mm'
     */
    SvgDrawVisitor(double w, double h, String units) {
        writeHeader(w, h, units);
    }

    private void writeHeader(double w, double h, String units) {
        sb.append("<?xml version='1.0' encoding='UTF-8'?>\n")
          .append("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n");
        sb.append("<svg")
          .append(" version='1.2'")
          .append(" xmlns='http://www.w3.org/2000/svg'")
          .append(" xmlns:xlink='http://www.w3.org/1999/xlink'")
          .append(" width='").append(toStr(w)).append(units).append('\'')
          .append(" height='").append(toStr(h)).append(units).append('\'')
          .append(" viewBox='0 0 ").append(toStr(w)).append(" ").append(toStr(h)).append("'")
          .append(">\n");
        indentLvl += 2;
        appendIdent();
        sb.append("<desc>Generated by the Chemistry Development Kit (http://github.com/cdk)</desc>\n");
    }

    private void appendIdent() {
        for (int i = 0; i < indentLvl; i++)
            sb.append(' ');
    }

    private double scaled(double num) {
        if (transform == null)
            return num;
        // presumed uniform x/y scaling
        return transform.getScaleX() * num;
    }

    private void transform(double[] points, int numPoints) {
        if (transform != null)
            transform.transform(points, 0, points, 0, numPoints);
    }

    private String toStr(double num) {
        return decimalFormat.format(num);
    }

    private void appendPoints(StringBuilder sb, double[] points, int numPoints) {
        switch (numPoints) {
            case 1:
                sb.append(decimalFormat.format(points[0]));
                sb.append(' ');
                sb.append(decimalFormat.format(points[1]));
                break;
            case 2:
                sb.append(decimalFormat.format(points[0]));
                sb.append(' ');
                sb.append(decimalFormat.format(points[1]));
                sb.append(' ');
                sb.append(decimalFormat.format(points[2]));
                sb.append(' ');
                sb.append(decimalFormat.format(points[3]));
                break;
            case 3:
                sb.append(decimalFormat.format(points[0]));
                sb.append(' ');
                sb.append(decimalFormat.format(points[1]));
                sb.append(' ');
                sb.append(decimalFormat.format(points[2]));
                sb.append(' ');
                sb.append(decimalFormat.format(points[3]));
                sb.append(' ');
                sb.append(decimalFormat.format(points[4]));
                sb.append(' ');
                sb.append(decimalFormat.format(points[5]));
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private void appendRelativePoints(StringBuilder sb, double[] points, double xBase, double yBase, int numPoints) {
        switch (numPoints) {
            case 1:
                sb.append(decimalFormat.format(points[0] - xBase));
                sb.append(' ');
                sb.append(decimalFormat.format(points[1] - yBase));
                break;
            case 2:
                sb.append(decimalFormat.format(points[0] - xBase));
                sb.append(' ');
                sb.append(decimalFormat.format(points[1] - yBase));
                sb.append(' ');
                sb.append(decimalFormat.format(points[2] - xBase));
                sb.append(' ');
                sb.append(decimalFormat.format(points[3] - yBase));
                break;
            case 3:
                sb.append(decimalFormat.format(points[0] - xBase));
                sb.append(' ');
                sb.append(decimalFormat.format(points[1] - yBase));
                sb.append(' ');
                sb.append(decimalFormat.format(points[2] - xBase));
                sb.append(' ');
                sb.append(decimalFormat.format(points[3] - yBase));
                sb.append(' ');
                sb.append(decimalFormat.format(points[4] - xBase));
                sb.append(' ');
                sb.append(decimalFormat.format(points[5] - yBase));
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    String toStr(Color col) {
        if (col.getAlpha() == 255) {
            return String.format("#%06X", (0xFFFFFF & col.getRGB()));
        } else {
            return String.format(Locale.ROOT, "rgba(%d,%d,%d,%.2f)", col.getRed(), col.getGreen(), col.getBlue(), col.getAlpha()/255d);
        }
    }

    @Override
    public void setFontManager(IFontManager fontManager) {
        // ignored
    }

    @Override
    public void setRendererModel(RendererModel model) {
        this.model = model;
    }

    /**
     * Pre-visit allows us to prepare the visitor for more optimal output.
     * Currently we
     * - find the most common stoke/fill/stroke-width values and set these as defaults
     *
     * @param elements elements to be visited
     */
    public void previsit(Collection<? extends IRenderingElement> elements) {
        Deque<IRenderingElement> queue = new ArrayDeque<>(2 * elements.size());
        queue.addAll(elements);

        FreqMap<Color> strokeFreq = new FreqMap<>();
        FreqMap<Color> fillFreq = new FreqMap<>();
        FreqMap<Double> strokeWidthFreq = new FreqMap<>();

        while (!queue.isEmpty()) {
            IRenderingElement element = queue.poll();
            // wrappers first
            if (element instanceof Bounds) {
                queue.add(((Bounds) element).root());
            } else if (element instanceof MarkedElement) {
                queue.add(((MarkedElement) element).element());
            } else if (element instanceof ElementGroup) {
                for (IRenderingElement child : (ElementGroup) element)
                    queue.add(child);
            } else if (element instanceof LineElement) {
                strokeFreq.add(((LineElement) element).color);
                strokeWidthFreq.add(scaled(((LineElement) element).width));
            } else if (element instanceof GeneralPath) {
                if (((GeneralPath) element).fill)
                    fillFreq.add(((GeneralPath) element).color);
            } else {
                // ignored
            }
        }

        if (!defaultsWritten) {
            defaultFill = fillFreq.getMostFrequent();
            defaultStroke = strokeFreq.getMostFrequent();
            Double strokeWidth = strokeWidthFreq.getMostFrequent();
            if (strokeWidth != null)
                defaultStrokeWidth = toStr(strokeWidth);
        }
    }

    private void visit(GeneralPath elem) {
        visit(null, null, elem);
    }

    private void visit(String id, String cls, GeneralPath elem) {
        appendIdent();
        if (elem.textString != null) {
            drawTextString(elem);
            return;
        }
        sb.append("<path");
        if (id != null)
            sb.append(" id='").append(id).append("'");
        if (cls != null)
            sb.append(" class='").append(cls).append("'");
        sb.append(" d='");
        double[] points = new double[6];
        double xCurr = 0, yCurr = 0;
        for (PathElement pelem : elem.elements) {
            pelem.points(points);
            switch (pelem.type) {
	            case Close:
	                sb.append("z");
	                xCurr = yCurr = 0;
	                break;
	            case LineTo:
	                transform(points, 1);
	                double dx = points[0] - xCurr;
	                double dy = points[1] - yCurr;
	                // horizontal and vertical lines can be even more compact
	                if (Math.abs(dx) < 0.01) {
	                    sb.append("v").append(toStr(dy));
	                } else if ((Math.abs(dy) < 0.01)) {
	                    sb.append("h").append(toStr(dx));
	                } else {
	                    sb.append("l");
	                    appendRelativePoints(sb, points, xCurr, yCurr, 1);
	                }
	                xCurr = round(points[0]);
	                yCurr = round(points[1]);
	                break;
	            case MoveTo:
	                // We have Move as always absolute
	                sb.append("M");
	                transform(points, 1);
	                appendPoints(sb, points, 1);
	                xCurr = round(points[0]);
	                yCurr = round(points[1]);
	                break;
	            case QuadTo:
	                sb.append("q");
	                transform(points, 2);
	                appendRelativePoints(sb, points, xCurr, yCurr, 2);
	                xCurr = round(points[2]);
	                yCurr = round(points[3]);
	                break;
	            case CubicTo:
	                sb.append("c");
	                transform(points, 3);
	                appendRelativePoints(sb, points, xCurr, yCurr, 3);
	                xCurr = round(points[4]);
	                yCurr = round(points[5]);
	                break;
	            }
        }
        sb.append("'");
        if (elem.fill) {
            sb.append(" stroke='none'");
            if (defaultFill == null || !defaultFill.equals(elem.color))
                sb.append(" fill='").append(toStr(elem.color)).append("'");
        } else {
            sb.append(" fill='none'");
            sb.append(" stroke='").append(toStr(elem.color)).append("'");
            sb.append(" stroke-width='").append(toStr(scaled(elem.stroke))).append("'");
        }
        sb.append("/>\n");
    }

    private void drawTextString(GeneralPath path) {
        Bounds b = new Bounds(path, transform);
        path.textString.setScale(transform);
        Point2D dxy = path.textString.getTextPosition(b.minX, b.minY);
        appendTextFont(path.textString.getText(), 
                dxy.getX(), dxy.getY(), 
                path.color,
                path.textString.getFont(), 0);
    }

    private void appendTextFont(String text, double px, double py, Color color, Font font, double stroke) {
        sb.append("<text");
        if (font != null) {
            int fstyle = font.getStyle();
            String style = null;
            String weight = null;
            if ((fstyle & Font.BOLD) != 0) {
                weight = "bold";
            }
            if ((fstyle & Font.ITALIC) != 0) {
                style = "italic";
            }
            sb.append(" font-family='").append(getCSSFontFamilyName(font.getFamily()));
            if (weight != null)
                sb.append("' font-weight='").append(weight);
            if (style != null)
                sb.append("' font-style='").append(style);
            sb.append("' stroke-width='").append(toStr(scaled(stroke)));
            sb.append("' font-size='").append(font.getSize2D()).append("'");
        }
        sb.append(" x='").append(toStr(px)).append("'");
        sb.append(" y='").append(toStr(py)).append("'");
        sb.append(" fill='").append(toStr(color)).append("'");
        // todo need font manager for scaling...
        sb.append(">");
        appendEscaped(sb, text);
        sb.append("</text>\n");
    }

    private static String getCSSFontFamilyName(String family) {
        family = family.toLowerCase();
        if (family.equals("sansserif") 
                || family.equals("helvetica") 
                || family.equals("dialog")
                || family.equals("dialoginput"))
            family = "Arial";
        else if (family.equals("monospaced"))
            family = "monospace";
        return family;
    }

    private void visit(LineElement elem) {
        visit(null, null, elem);
    }

    private void visit(String id, String cls, LineElement elem) {
        double[] points = new double[]{elem.firstPointX, elem.firstPointY, elem.secondPointX, elem.secondPointY};
        transform(points, 2);
        appendIdent();
        sb.append("<line");
        if (id != null) sb.append(" id='").append(id).append("'");
        if (cls != null) sb.append(" class='").append(cls).append("'");
        sb.append(" x1='").append(toStr(points[0])).append("'")
          .append(" y1='").append(toStr(points[1])).append("'")
          .append(" x2='").append(toStr(points[2])).append("'")
          .append(" y2='").append(toStr(points[3])).append("'");
        if (defaultStroke == null || !defaultStroke.equals(elem.color))
            sb.append(" stroke='").append(toStr(elem.color)).append("'");
        if (defaultStroke == null || !defaultStrokeWidth.equals(toStr(scaled(elem.width))))
            sb.append(" stroke-width='").append(toStr(scaled(elem.width))).append("'");
        sb.append("/>\n");
    }

    private void visit(MarkedElement elem) {
        String id = elem.getId();
        List<String> classes = elem.getClasses();
        String cls = classes.isEmpty() ? null : String.join(" ", classes);

        IRenderingElement marked = elem.element();

        // unpack singletons
        while (marked instanceof ElementGroup) {
            Iterator<IRenderingElement> iter = ((ElementGroup) marked).iterator();
            if (iter.hasNext())
                marked = iter.next();
            else
                marked = null;
            if (iter.hasNext())
                marked = null; // non-singleton
        }

        if (marked == null)
            marked = elem.element();

        // we try to
        if (marked instanceof LineElement) {
            visit(id, cls, (LineElement) marked);
        } else if (marked instanceof GeneralPath) {
            visit(id, cls, (GeneralPath) marked);
        } else {
            appendIdent();
            sb.append("<g");
            if (id != null)
                sb.append(" id='").append(elem.getId()).append("'");
            if (cls != null)
                sb.append(" class='").append(cls).append("'");
            sb.append(">\n");
            indentLvl += 2;
            visit(marked);
            indentLvl -= 2;
            appendIdent();
            sb.append("</g>\n");
        }
    }

    private void visit(RectangleElement elem) {
        appendIdent();
        double[] points = new double[]{elem.xCoord, elem.yCoord};
        transform(points, 1);
        double height = scaled(elem.height);
        sb.append("<rect");
        sb.append(" x='").append(toStr(points[0])).append("'");
        sb.append(" y='").append(toStr(points[1]-height)).append("'");
        sb.append(" width='").append(toStr(scaled(elem.width))).append("'");
        sb.append(" height='").append(toStr(height)).append("'");
        if (elem.filled) {
            sb.append(" fill='").append(toStr(elem.color)).append("'");
            sb.append(" stroke='none'");
        } else {
            sb.append(" fill='none'");
            sb.append(" stroke='").append(toStr(elem.color)).append("'");
        }
        sb.append("/>\n");
    }

    private void visit(OvalElement elem) {
        appendIdent();
        double[] points = new double[]{elem.xCoord, elem.yCoord};
        transform(points, 1);
        sb.append("<ellipse");
        sb.append(" cx='").append(toStr(points[0])).append("'");
        sb.append(" cy='").append(toStr(points[1])).append("'");
        sb.append(" rx='").append(toStr(scaled(elem.radius))).append("'");
        sb.append(" ry='").append(toStr(scaled(elem.radius))).append("'");
        if (elem.fill) {
            sb.append(" fill='").append(toStr(elem.color)).append("'");
            sb.append(" stroke='none'");
        } else {
            sb.append(" fill='none'");
            sb.append(" stroke='").append(toStr(elem.color)).append("'");
        }
        sb.append("/>\n");
    }

    private void appendEscaped(StringBuilder sb, String text)
    {
        for (int i=0; i<text.length(); i++) {
            char ch = text.charAt(i);
            switch (ch) {
                case '\n':
                case '\r':
                case '\t': sb.append(ch); break;
                case '<':  sb.append("&lt;"); break;
                case '>':  sb.append("&gt;"); break;
                case '&':  sb.append("&amp;"); break;
                default:
                    if (ch < 0x1f)
                        sb.append("\uFFFD"); // control chars
                    else if (ch > 0xFFFD)
                        sb.append("\uFFFD");
                    else
                        sb.append(ch);
                    break;
            }
        }
    }

    private void visit(TextElement elem) {
        appendIdent();
        double[] points = new double[]{elem.xCoord, elem.yCoord};
        transform(points, 1);
        sb.append("<text ");
        sb.append(" x='").append(toStr(points[0])).append("'");
        sb.append(" y='").append(toStr(points[1])).append("'");
        sb.append(" fill='").append(toStr(elem.color)).append("'");
        sb.append(" text-anchor='middle'");
        // todo need font manager for scaling...
        sb.append(">");
        appendEscaped(sb, elem.text);
        sb.append("</text>\n");
    }

    @Override
    public void visit(final IRenderingElement root) {

        if (!defaultsWritten) {
            appendIdent();
            sb.append("<g")
              .append(" stroke-linecap='round'")
              .append(" stroke-linejoin='round'");
            if (defaultStroke != null)
                sb.append(" stroke='").append(toStr(defaultStroke)).append("'");
            if (defaultStrokeWidth != null)
                sb.append(" stroke-width='").append(defaultStrokeWidth).append("'");
            if (defaultFill != null)
                sb.append(" fill='").append(toStr(defaultFill)).append("'");
            sb.append(">\n");
            indentLvl += 2;
            defaultsWritten = true;
        }

        Deque<IRenderingElement> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            IRenderingElement elem = queue.poll();
            if (elem instanceof ElementGroup) {
                for (IRenderingElement child : (ElementGroup) elem)
                    queue.add(child);
            } else if (elem instanceof Bounds) {
                queue.add(((Bounds) elem).root());
            } else if (elem instanceof MarkedElement) {
                if (model != null && model.get(RendererModel.MarkedOutput.class)) {
                    visit(((MarkedElement) elem));
                } else {
                    visit(((MarkedElement) elem).element());
                }
            } else if (elem instanceof LineElement) {
                visit((LineElement) elem);
            } else if (elem instanceof GeneralPath) {
                visit((GeneralPath) elem);
            } else if (elem instanceof RectangleElement) {
                visit((RectangleElement) elem);
            } else if (elem instanceof OvalElement) {
                visit((OvalElement) elem);
            } else if (elem instanceof TextElement) {
                visit((TextElement) elem);
            } else {
                System.err.println(elem.getClass() + " rendering element is not supported by"
                                   + " this visitor, parts of the depiction may missing!");
            }
        }
    }

    @Override
    public void setTransform(AffineTransform transform) {
        this.transform = transform;
    }

    @Override
    public String toString() {
        if (defaultsWritten)
            return sb + "  </g>\n</svg>\n";
        return sb + "</svg>\n";
    }

    private static final class Counter {
        private int count = 1;
    }

    private static final class FreqMap<T> {
        final Map<T, Counter> map = new HashMap<>();

        public FreqMap() {
        }

        void add(T obj) {
            Counter counter = map.get(obj);
            if (counter == null) {
                map.put(obj, new Counter());
            } else {
                counter.count++;
            }
        }

        T getMostFrequent() {
            if (map.isEmpty()) {
                return null;
            } else {
                T maxKey = null;
                for (Map.Entry<T, Counter> e : map.entrySet()) {
                    if (maxKey == null || e.getValue().count > map.get(maxKey).count)
                        maxKey = e.getKey();
                }
                return maxKey;
            }
        }
    }
}
