import javax.swing.*;import java.util.*;
public class RadioKeysModern{public static void main(String[] a)throws Exception{SwingUtilities.invokeAndWait(()->{
 JRadioButton r=new JRadioButton("a");
 for(int w:new int[]{JComponent.WHEN_FOCUSED,JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT}){InputMap im=r.getInputMap(w);KeyStroke[] k=im.allKeys();if(k==null)continue;Arrays.sort(k,Comparator.comparing(KeyStroke::toString));StringBuilder sb=new StringBuilder("w"+w+": ");for(KeyStroke s:k)sb.append(s+"="+im.get(s)+";");System.out.println(sb);}
 JToggleButton t=new JToggleButton("a");
 InputMap im=t.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);KeyStroke[] k=im.allKeys();StringBuilder sb=new StringBuilder("tog: ");for(KeyStroke s:k)sb.append(s+"="+im.get(s)+";");System.out.println(sb);
 ButtonGroup g=new ButtonGroup();JRadioButton b1=new JRadioButton("1"),b2=new JRadioButton("2"),b3=new JRadioButton("3");g.add(b1);g.add(b2);g.add(b3);b1.setSelected(true);
 JPanel p=new JPanel();p.add(b1);p.add(b2);p.add(b3);
 java.awt.event.ActionEvent e=new java.awt.event.ActionEvent(b1,0,"Next");
 b1.getActionMap().get("Next").actionPerformed(e);System.out.println(b1.isSelected()+" "+b2.isSelected()+" "+b3.isSelected());
 b2.getActionMap().get("Next").actionPerformed(new java.awt.event.ActionEvent(b2,0,"Next"));System.out.println(b1.isSelected()+" "+b2.isSelected()+" "+b3.isSelected());
 b3.getActionMap().get("Next").actionPerformed(new java.awt.event.ActionEvent(b3,0,"Next"));System.out.println(b1.isSelected()+" "+b2.isSelected()+" "+b3.isSelected());
 b1.getActionMap().get("Previous").actionPerformed(new java.awt.event.ActionEvent(b1,0,"Previous"));System.out.println(b1.isSelected()+" "+b2.isSelected()+" "+b3.isSelected());
 JCheckBox cb=new JCheckBox("c");cb.getActionMap().get("pressed").actionPerformed(new java.awt.event.ActionEvent(cb,0,"pressed"));System.out.println(cb.getModel().isPressed()+" "+cb.getModel().isArmed()+" "+cb.isSelected());
 cb.getActionMap().get("released").actionPerformed(new java.awt.event.ActionEvent(cb,0,"released"));System.out.println(cb.getModel().isPressed()+" "+cb.getModel().isArmed()+" "+cb.isSelected());
});}}
