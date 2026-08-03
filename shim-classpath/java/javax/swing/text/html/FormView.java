/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text.html;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.ComponentView;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;

public class FormView extends ComponentView implements ActionListener {
  public static final String SUBMIT = new String("Submit Query");
  public static final String RESET = new String("Reset");
  private short maxIsPreferred;

  public FormView(Element elem) {
    super(elem);
  }

  protected Component createComponent() {
    AttributeSet attr = getElement().getAttributes();
    HTML.Tag t = (HTML.Tag) attr.getAttribute(StyleConstants.NameAttribute);
    JComponent c = null;
    Object model = attr.getAttribute(StyleConstants.ModelAttribute);
    if (t == HTML.Tag.INPUT) {
      c = createInputComponent(attr, model);
    } else if (t == HTML.Tag.SELECT) {
      if (model instanceof OptionListModel) {
        OptionListModel<?> m = (OptionListModel<?>) model;
        JList<Object> list = new JList<Object>(castList(m));
        int size = 0;
        Object sz = attr.getAttribute(HTML.Attribute.SIZE);
        if (sz != null) {
          try {
            size = Integer.parseInt(sz.toString().trim());
          } catch (NumberFormatException e) {
            size = 0;
          }
        }
        list.setVisibleRowCount(size);
        list.setSelectionModel(m);
        c = new JScrollPane(list);
      } else if (model instanceof ComboBoxModel) {
        c = new JComboBox<Object>(castCombo((ComboBoxModel<?>) model));
        maxIsPreferred = 3;
      }
    } else if (t == HTML.Tag.TEXTAREA) {
      JTextArea area = new JTextArea((Document) model);
      int rows = intOf(attr, HTML.Attribute.ROWS, 1);
      int cols = intOf(attr, HTML.Attribute.COLS, 20);
      maxIsPreferred = 3;
      area.setRows(rows);
      area.setColumns(cols);
      c = new JScrollPane(area, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    }
    if (c != null) {
      c.setAlignmentY(1.0f);
    }
    return c;
  }

  @SuppressWarnings("unchecked")
  private static ListModel<Object> castList(ListModel<?> m) { return (ListModel<Object>) m; }

  @SuppressWarnings("unchecked")
  private static ComboBoxModel<Object> castCombo(ComboBoxModel<?> m) { return (ComboBoxModel<Object>) m; }

  private JComponent createInputComponent(AttributeSet attr, Object model) {
    Object typeAttr = attr.getAttribute(HTML.Attribute.TYPE);
    String type = typeAttr == null ? "text" : typeAttr.toString();
    String value = (String) attr.getAttribute(HTML.Attribute.VALUE);
    JComponent c = null;
    if (type.equals("submit") || type.equals("reset")) {
      String def = type.equals("submit") ? UIManager.getString("FormView.submitButtonText")
          : UIManager.getString("FormView.resetButtonText");
      if (def == null) {
        def = type.equals("submit") ? SUBMIT : RESET;
      }
      JButton button = new JButton(value != null ? value : def);
      if (model != null) {
        button.setModel((ButtonModel) model);
      }
      button.addActionListener(this);
      c = button;
      maxIsPreferred = 3;
    } else if (type.equals("image")) {
      String srcAtt = (String) attr.getAttribute(HTML.Attribute.SRC);
      JButton button;
      try {
        URL base = ((HTMLDocument) getDocument()).getBase();
        URL iconUrl = new URL(base, srcAtt);
        ImageIcon icon = new ImageIcon(iconUrl);
        button = icon.getIconWidth() > 0 ? new JButton(icon) : new JButton(srcAtt);
      } catch (MalformedURLException e) {
        button = new JButton(srcAtt);
      }
      if (model != null) {
        button.setModel((ButtonModel) model);
      }
      button.addMouseListener(new MouseEventListener());
      c = button;
      maxIsPreferred = 3;
    } else if (type.equals("checkbox")) {
      c = new JCheckBox();
      if (model != null) {
        ((JCheckBox) c).setModel((ButtonModel) model);
      }
      ((JCheckBox) c).setSelected(attr.getAttribute(HTML.Attribute.CHECKED) != null);
      maxIsPreferred = 3;
    } else if (type.equals("radio")) {
      JRadioButton radio = new JRadioButton();
      Object name = attr.getAttribute(HTML.Attribute.NAME);
      if (name != null) {
        Document doc = getDocument();
        @SuppressWarnings("unchecked")
        Map<String, ButtonGroup> groups = (Map<String, ButtonGroup>) doc.getProperty("__radioGroups__");
        if (groups == null) {
          groups = new HashMap<String, ButtonGroup>();
          doc.putProperty("__radioGroups__", groups);
        }
        ButtonGroup g = groups.get(name.toString());
        if (g == null) {
          g = new ButtonGroup();
          groups.put(name.toString(), g);
        }
        g.add(radio);
      }
      radio.setSelected(attr.getAttribute(HTML.Attribute.CHECKED) != null);
      c = radio;
      maxIsPreferred = 3;
    } else if (type.equals("text")) {
      int size = intOf(attr, HTML.Attribute.SIZE, -1);
      JTextField tf = size > 0 ? new JTextField(size) : new JTextField(20);
      limitLength(tf, attr);
      if (value != null) {
        tf.setText(value);
      }
      tf.addActionListener(this);
      c = tf;
      maxIsPreferred = 2;
    } else if (type.equals("password")) {
      int size = intOf(attr, HTML.Attribute.SIZE, -1);
      JPasswordField pf = size > 0 ? new JPasswordField(size) : new JPasswordField(20);
      limitLength(pf, attr);
      if (value != null) {
        pf.setText(value);
      }
      pf.addActionListener(this);
      c = pf;
      maxIsPreferred = 2;
    } else if (type.equals("file")) {
      JTextField tf = new JTextField(20);
      tf.setEditable(false);
      JButton browse = new JButton(UIManager.getString("FormView.browseFileButtonText") != null
          ? UIManager.getString("FormView.browseFileButtonText") : "Browse...");
      Box box = Box.createHorizontalBox();
      box.add(tf);
      box.add(Box.createHorizontalStrut(5));
      box.add(browse);
      c = (JComponent) box;
      maxIsPreferred = 3;
    }
    return c;
  }

  private static void limitLength(JTextField tf, AttributeSet attr) {
    final int max = intOf(attr, HTML.Attribute.MAXLENGTH, -1);
    if (max > 0) {
      PlainDocument doc = new PlainDocument();
      doc.setDocumentFilter(new javax.swing.text.DocumentFilter() {
        public void insertString(FilterBypass fb, int offset, String str, AttributeSet a)
            throws javax.swing.text.BadLocationException {
          if (str != null && fb.getDocument().getLength() + str.length() <= max) {
            super.insertString(fb, offset, str, a);
          }
        }

        public void replace(FilterBypass fb, int offset, int length, String str, AttributeSet a)
            throws javax.swing.text.BadLocationException {
          int now = fb.getDocument().getLength() - length + (str == null ? 0 : str.length());
          if (now <= max) {
            super.replace(fb, offset, length, str, a);
          }
        }
      });
      tf.setDocument(doc);
    }
  }

  private static int intOf(AttributeSet attr, HTML.Attribute key, int def) {
    Object v = attr.getAttribute(key);
    try {
      return v == null ? def : Integer.parseInt(v.toString().trim());
    } catch (NumberFormatException e) {
      return def;
    }
  }

  public float getMaximumSpan(int axis) {
    switch (axis) {
      case View.X_AXIS:
        if (maxIsPreferred > 1) {
          return super.getMaximumSpan(axis) == 0 ? 0 : getPreferredSpan(axis);
        }
        return super.getMaximumSpan(axis);
      case View.Y_AXIS:
        if (maxIsPreferred > 2) {
          return getPreferredSpan(axis);
        }
        return super.getMaximumSpan(axis);
      default:
        return super.getMaximumSpan(axis);
    }
  }

  public void actionPerformed(ActionEvent evt) {
    Element element = getElement();
    StringBuilder dataBuffer = new StringBuilder();
    HTMLDocument doc = (HTMLDocument) getDocument();
    AttributeSet attr = element.getAttributes();
    String type = (String) attr.getAttribute(HTML.Attribute.TYPE);
    if (type != null && type.equals("reset")) {
      resetForm();
    } else {
      if (type != null && type.equals("submit") && attr.getAttribute(HTML.Attribute.NAME) != null) {
        appendBuffer(dataBuffer, (String) attr.getAttribute(HTML.Attribute.NAME), (String) attr.getAttribute(HTML.Attribute.VALUE));
      }
      if (type == null || !type.equals("image")) {
        submitData(constructData(dataBuffer));
      }
    }
  }

  protected void submitData(String data) {
    Element form = getFormElement();
    if (form == null) {
      return;
    }
    AttributeSet attrs = form.getAttributes();
    HTMLDocument doc = (HTMLDocument) form.getDocument();
    URL base = doc.getBase();
    String target = (String) attrs.getAttribute(HTML.Attribute.TARGET);
    if (target == null) {
      target = "_self";
    }
    String method = (String) attrs.getAttribute(HTML.Attribute.METHOD);
    if (method == null) {
      method = "GET";
    }
    method = method.toLowerCase();
    boolean isPostMethod = method.equals("post");
    String action = (String) attrs.getAttribute(HTML.Attribute.ACTION);
    URL actionURL;
    try {
      if (action == null) {
        actionURL = base == null ? null : new URL(base.getProtocol(), base.getHost(), base.getPort(), base.getFile());
      } else {
        actionURL = new URL(base, action);
      }
      if (!isPostMethod && actionURL != null) {
        actionURL = new URL(actionURL + "?" + data);
      }
    } catch (MalformedURLException e) {
      actionURL = null;
    }
    final JEditorPane c = (JEditorPane) getContainer();
    HTMLEditorKit kit = (HTMLEditorKit) c.getEditorKit();
    FormSubmitEvent formEvent = null;
    if (!kit.isAutoFormSubmission() || doc.isFrameDocument()) {
      FormSubmitEvent.MethodType methodType = isPostMethod ? FormSubmitEvent.MethodType.POST
          : FormSubmitEvent.MethodType.GET;
      formEvent = new FormSubmitEvent(FormView.this, HyperlinkEvent.EventType.ACTIVATED, actionURL, form, target,
          methodType, data);
    }
    final FormSubmitEvent fse = formEvent;
    final URL url = actionURL;
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if (fse != null) {
          c.fireHyperlinkUpdate(fse);
        } else if (url != null) {
          try {
            c.setPage(url);
          } catch (IOException e) {
            UIManager.getLookAndFeel().provideErrorFeedback(c);
          }
        }
      }
    });
  }

  protected void imageSubmit(String imageData) {
    StringBuilder dataBuffer = new StringBuilder();
    Element elem = getElement();
    HTMLDocument hdoc = (HTMLDocument) elem.getDocument();
    appendBuffer(dataBuffer, (String) elem.getAttributes().getAttribute(HTML.Attribute.NAME), imageData);
    submitData(constructData(dataBuffer));
  }

  private Element getFormElement() {
    Element e = getElement();
    while (e != null) {
      if (e.getAttributes().getAttribute(StyleConstants.NameAttribute) == HTML.Tag.FORM) {
        return e;
      }
      e = e.getParentElement();
    }
    return null;
  }

  private String constructData(StringBuilder buffer) {
    Element formE = getFormElement();
    StringBuilder out = new StringBuilder();
    if (formE != null) {
      List<View> views = new ArrayList<View>();
      collect(getContainerRoot(), views, formE);
      for (View v : views) {
        loadElementDataIntoBuffer((FormView) v, out);
      }
    }
    if (buffer.length() > 0) {
      if (out.length() > 0) {
        out.append('&');
      }
      out.append(buffer);
    }
    return out.toString();
  }

  private View getContainerRoot() {
    Container c = getContainer();
    if (c instanceof JEditorPane) {
      return ((JEditorPane) c).getUI().getRootView((JEditorPane) c);
    }
    return null;
  }

  private void collect(View v, List<View> out, Element form) {
    if (v == null) {
      return;
    }
    if (v instanceof FormView && inside(v.getElement(), form)) {
      out.add(v);
    }
    for (int i = 0; i < v.getViewCount(); i++) {
      collect(v.getView(i), out, form);
    }
  }

  private static boolean inside(Element e, Element form) {
    return e.getStartOffset() >= form.getStartOffset() && e.getEndOffset() <= form.getEndOffset();
  }

  private void loadElementDataIntoBuffer(FormView view, StringBuilder buffer) {
    AttributeSet attr = view.getElement().getAttributes();
    String name = (String) attr.getAttribute(HTML.Attribute.NAME);
    if (name == null) {
      return;
    }
    Object tagObj = attr.getAttribute(StyleConstants.NameAttribute);
    Component comp = view.getComponent();
    String value = null;
    if (tagObj == HTML.Tag.INPUT) {
      String type = (String) attr.getAttribute(HTML.Attribute.TYPE);
      if (type == null) {
        type = "text";
      }
      if (type.equals("text") || type.equals("password")) {
        JTextField tf = (JTextField) comp;
        if (tf != null) {
          value = type.equals("password") ? new String(((JPasswordField) tf).getPassword()) : tf.getText();
        }
      } else if (type.equals("checkbox") || type.equals("radio")) {
        if (comp instanceof AbstractButton && ((AbstractButton) comp).isSelected()) {
          value = (String) attr.getAttribute(HTML.Attribute.VALUE);
          if (value == null) {
            value = "on";
          }
        }
      } else if (type.equals("hidden")) {
        value = (String) attr.getAttribute(HTML.Attribute.VALUE);
        if (value == null) {
          value = "";
        }
      }
      if (value != null) {
        appendBuffer(buffer, name, value);
      }
    } else if (tagObj == HTML.Tag.TEXTAREA) {
      JTextArea area = (JTextArea) ((JScrollPane) comp).getViewport().getView();
      appendBuffer(buffer, name, area.getText());
    } else if (tagObj == HTML.Tag.SELECT) {
      Object m = attr.getAttribute(StyleConstants.ModelAttribute);
      if (m instanceof OptionListModel) {
        OptionListModel<?> model = (OptionListModel<?>) m;
        for (int i = 0; i < model.getSize(); i++) {
          if (model.isSelectedIndex(i)) {
            appendBuffer(buffer, name, ((Option) model.getElementAt(i)).getValue());
          }
        }
      } else if (m instanceof ComboBoxModel) {
        Object item = ((ComboBoxModel<?>) m).getSelectedItem();
        if (item instanceof Option) {
          appendBuffer(buffer, name, ((Option) item).getValue());
        }
      }
    }
  }

  private void appendBuffer(StringBuilder buffer, String name, String value) {
    if (buffer.length() > 0) {
      buffer.append('&');
    }
    String encodedName = URLEncoder.encode(name, java.nio.charset.StandardCharsets.UTF_8);
    buffer.append(encodedName);
    buffer.append('=');
    String encodedValue = URLEncoder.encode(value == null ? "" : value, java.nio.charset.StandardCharsets.UTF_8);
    buffer.append(encodedValue);
  }

  private void resetForm() {
    Element formE = getFormElement();
    if (formE == null) {
      return;
    }
    List<View> views = new ArrayList<View>();
    collect(getContainerRoot(), views, formE);
    for (View v : views) {
      FormView fv = (FormView) v;
      Component comp = fv.getComponent();
      AttributeSet attr = fv.getElement().getAttributes();
      if (comp instanceof JPasswordField) {
        ((JPasswordField) comp).setText((String) attr.getAttribute(HTML.Attribute.VALUE));
      } else if (comp instanceof JTextField) {
        String val = (String) attr.getAttribute(HTML.Attribute.VALUE);
        ((JTextField) comp).setText(val == null ? "" : val);
      } else if (comp instanceof AbstractButton && attr.getAttribute(HTML.Attribute.TYPE) != null
          && !attr.getAttribute(HTML.Attribute.TYPE).equals("submit")
          && !attr.getAttribute(HTML.Attribute.TYPE).equals("reset")) {
        ((AbstractButton) comp).setSelected(attr.getAttribute(HTML.Attribute.CHECKED) != null);
      } else if (comp instanceof JComboBox) {
        Object m = attr.getAttribute(StyleConstants.ModelAttribute);
        if (m instanceof OptionComboBoxModel && ((OptionComboBoxModel<?>) m).getInitialSelection() != null) {
          ((OptionComboBoxModel<?>) m).setSelectedItem(((OptionComboBoxModel<?>) m).getInitialSelection());
        }
      }
    }
  }

  private class MouseEventListener extends java.awt.event.MouseAdapter {
    public void mouseReleased(java.awt.event.MouseEvent evt) {
      String imageData = evt.getX() + ":" + evt.getY();
      imageSubmit(imageData);
    }
  }
}
