/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import javax.swing.*; import java.beans.*; import java.util.*;
public class BeanInfoSwingParity { public static void main(String[] a) throws Exception { for (Class<?> c : new Class<?>[]{JLabel.class, JButton.class, JPanel.class, JTextField.class, JTable.class, JTree.class, JSlider.class, JTabbedPane.class, JComboBox.class}) { System.out.println("== "+c.getSimpleName()); for (PropertyDescriptor pd : Introspector.getBeanInfo(c).getPropertyDescriptors()) System.out.println(pd.getName()+" r="+(pd.getReadMethod()!=null)+" w="+(pd.getWriteMethod()!=null)+" h="+pd.isHidden()+" e="+pd.isExpert()+" p="+pd.isPreferred()+" t="+pd.getValue("transient")+" b="+pd.isBound()+" c="+pd.isConstrained()+" ed="+(pd.getPropertyEditorClass()!=null)); } } }
