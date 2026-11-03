/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.modelmbean;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.management.Attribute;
import javax.management.AttributeChangeNotification;
import javax.management.AttributeChangeNotificationFilter;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.Descriptor;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeErrorException;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeOperationsException;
import javax.management.loading.ClassLoaderRepository;

public class RequiredModelMBean implements ModelMBean, MBeanRegistration, NotificationEmitter {
  private ModelMBeanInfo modelMBeanInfo;
  private final NotificationBroadcasterSupport generic = new NotificationBroadcasterSupport();
  private final NotificationBroadcasterSupport attributeBroadcaster = new NotificationBroadcasterSupport();
  private Object managedResource = null;
  private boolean registered = false;
  private transient MBeanServer server = null;
  private long sequenceNumber = 1;

  public RequiredModelMBean() throws MBeanException, RuntimeOperationsException {
    modelMBeanInfo = createDefaultModelMBeanInfo();
  }

  public RequiredModelMBean(ModelMBeanInfo mbi) throws MBeanException, RuntimeOperationsException {
    if (mbi == null) {
      throw new RuntimeOperationsException(new IllegalArgumentException("ModelMBeanInfo must not be null"),
          "Exception occurred trying to initialize the ModelMBeanInfo of the RequiredModelMBean");
    }
    modelMBeanInfo = createModelMBeanInfo(mbi);
  }

  private static ModelMBeanInfo createDefaultModelMBeanInfo() {
    return new ModelMBeanInfoSupport("javax.management.modelmbean.RequiredModelMBean", "Default ModelMBean", null,
        null, null, null);
  }

  private static ModelMBeanInfo createModelMBeanInfo(ModelMBeanInfo mbi) {
    return new ModelMBeanInfoSupport(mbi);
  }

  public void setModelMBeanInfo(ModelMBeanInfo mbi) throws MBeanException, RuntimeOperationsException {
    if (mbi == null) {
      throw new RuntimeOperationsException(new IllegalArgumentException("ModelMBeanInfo must not be null"),
          "Exception occurred trying to initialize the ModelMBeanInfo of the RequiredModelMBean");
    }
    if (registered) {
      throw new RuntimeOperationsException(new IllegalStateException("MBean is registered in the MBeanServer"),
          "Exception occurred trying to set the ModelMBeanInfo of the RequiredModelMBean");
    }
    modelMBeanInfo = createModelMBeanInfo(mbi);
  }

  public void setManagedResource(Object mr, String mr_type) throws MBeanException, RuntimeOperationsException,
      InstanceNotFoundException, InvalidTargetObjectTypeException {
    if (mr_type == null || !mr_type.equalsIgnoreCase("objectreference")) {
      throw new InvalidTargetObjectTypeException(mr_type);
    }
    managedResource = mr;
  }

  public MBeanInfo getMBeanInfo() {
    if (modelMBeanInfo == null) {
      return null;
    }
    return (MBeanInfo) ((ModelMBeanInfoSupport) modelMBeanInfo).clone();
  }

  public String getClassName() {
    return modelMBeanInfo.getClassName();
  }

  protected ClassLoaderRepository getClassLoaderRepository() {
    return javax.management.MBeanServerFactory.getClassLoaderRepository(server);
  }

  private static boolean intValue(Object o, long dflt, long[] out) {
    if (o instanceof Number) {
      out[0] = ((Number) o).longValue();
      return true;
    }
    if (o instanceof String) {
      try {
        out[0] = Long.parseLong((String) o);
        return true;
      } catch (NumberFormatException e) {
        return false;
      }
    }
    return false;
  }

  public Object invoke(String opName, Object[] opArgs, String[] sig) throws MBeanException, ReflectionException {
    if (opName == null) {
      throw new RuntimeOperationsException(new IllegalArgumentException("Method name must not be null"),
          "An exception occurred while trying to invoke a method on a RequiredModelMBean");
    }
    String opClassName = null;
    String opMethodName;
    int opSplitter = opName.lastIndexOf('.');
    if (opSplitter > 0) {
      opClassName = opName.substring(0, opSplitter);
      opMethodName = opName.substring(opSplitter + 1);
    } else {
      opMethodName = opName;
    }
    opSplitter = opMethodName.indexOf("(");
    if (opSplitter > 0) {
      opMethodName = opMethodName.substring(0, opSplitter);
    }
    Descriptor opDescr = null;
    ModelMBeanOperationInfo opInfo = null;
    for (MBeanOperationInfo oi : modelMBeanInfo.getOperations()) {
      if (opMethodName.equals(oi.getName()) && signatureMatches(oi, sig)) {
        opInfo = (ModelMBeanOperationInfo) oi;
        break;
      }
    }
    Object target = managedResource;
    String targetType = "objectReference";
    String mappedName = opMethodName;
    if (opInfo != null) {
      opDescr = opInfo.getDescriptor();
      Object tobj = opDescr.getFieldValue("targetObject");
      if (tobj != null) {
        target = tobj;
      }
      Object mapped = opDescr.getFieldValue("name");
      if (mapped instanceof String) {
        mappedName = (String) mapped;
      }
    }
    if (opInfo == null) {
      throw new MBeanException(new ServiceNotFound("Operation " + opName + " not in ModelMBeanInfo"),
          "Operation " + opName + " not in ModelMBeanInfo");
    }
    Method m = null;
    Object invokeTarget = null;
    Object explicitTarget = opDescr == null ? null : opDescr.getFieldValue("targetObject");
    if (explicitTarget == null) {
      m = findMethod(RequiredModelMBean.class, opMethodName, sig);
      invokeTarget = this;
    }
    if (m == null && target != null) {
      m = findMethod(target.getClass(), mappedName, sig);
      invokeTarget = target;
    }
    if (m == null) {
      throw new ReflectionException(new NoSuchMethodException(opName),
          "The method " + opName + " could not be found on the managed resource");
    }
    try {
      return m.invoke(invokeTarget, opArgs);
    } catch (InvocationTargetException e) {
      Throwable t = e.getTargetException();
      if (t instanceof RuntimeException) {
        throw new MBeanException((RuntimeException) t, "RuntimeException thrown in RequiredModelMBean while trying to invoke operation " + opName);
      } else if (t instanceof Error) {
        throw new RuntimeErrorException((Error) t, "Error occurred in RequiredModelMBean while trying to invoke operation " + opName);
      } else {
        throw new MBeanException((Exception) t, "Exception thrown in RequiredModelMBean while trying to invoke operation " + opName);
      }
    } catch (IllegalAccessException e) {
      throw new ReflectionException(e, e.toString());
    } catch (IllegalArgumentException e) {
      throw new ReflectionException(e, e.toString());
    }
  }

  private static final class ServiceNotFound extends javax.management.ServiceNotFoundException {
    private static final long serialVersionUID = 1L;

    ServiceNotFound(String msg) {
      super(msg);
    }
  }

  private static boolean signatureMatches(MBeanOperationInfo oi, String[] sig) {
    MBeanParameterInfo[] ps = oi.getSignature();
    int n = sig == null ? 0 : sig.length;
    if (ps.length != n) {
      return false;
    }
    for (int i = 0; i < n; i++) {
      if (!ps[i].getType().equals(sig[i])) {
        return false;
      }
    }
    return true;
  }

  private static Method findMethod(Class<?> c, String name, String[] sig) {
    int n = sig == null ? 0 : sig.length;
    for (Method m : c.getMethods()) {
      if (!m.getName().equals(name) || m.getParameterTypes().length != n) {
        continue;
      }
      boolean ok = true;
      Class<?>[] pt = m.getParameterTypes();
      for (int i = 0; i < n; i++) {
        if (!pt[i].getName().equals(sig[i])) {
          ok = false;
          break;
        }
      }
      if (ok) {
        return m;
      }
    }
    return null;
  }

  private Object resolveGetter(String attrName, ModelMBeanAttributeInfo ai) throws MBeanException, ReflectionException {
    Descriptor d = ai.getDescriptor();
    Object getMethod = d.getFieldValue("getMethod");
    Object value = d.getFieldValue("value");
    Object dflt = d.getFieldValue("default");
    if (getMethod == null) {
      return value != null ? value : dflt;
    }
    long[] limit = new long[1];
    long[] stamp = new long[1];
    boolean haveLimit = intValue(d.getFieldValue("currencyTimeLimit"), 0, limit);
    boolean haveStamp = intValue(d.getFieldValue("lastUpdatedTimeStamp"), 0, stamp);
    if (value != null && haveLimit && haveStamp) {
      long now = System.currentTimeMillis();
      if (limit[0] < 0) {
        return value;
      }
      if (limit[0] > 0 && now < stamp[0] + limit[0] * 1000L) {
        return value;
      }
    }
    Object result = invoke((String) getMethod, null, null);
    if (haveLimit && limit[0] != 0) {
      d.setField("value", result);
      d.setField("lastUpdatedTimeStamp", Long.toString(System.currentTimeMillis()));
      try {
        modelMBeanInfo.setDescriptor(d, "attribute");
      } catch (MBeanException e) {
        throw e;
      }
    }
    return result;
  }

  public Object getAttribute(String attrName) throws AttributeNotFoundException, MBeanException, ReflectionException {
    if (attrName == null) {
      throw new RuntimeOperationsException(new IllegalArgumentException("attributeName must not be null"),
          "Exception occurred trying to get attribute of a RequiredModelMBean");
    }
    ModelMBeanAttributeInfo ai = modelMBeanInfo.getAttribute(attrName);
    if (ai == null) {
      throw new AttributeNotFoundException("getAttribute failed: ModelMBeanAttributeInfo not found for " + attrName);
    }
    return resolveGetter(attrName, ai);
  }

  public AttributeList getAttributes(String[] attrNames) {
    if (attrNames == null) {
      throw new RuntimeOperationsException(new IllegalArgumentException("attributeNames must not be null"),
          "Exception occurred trying to get attributes of a RequiredModelMBean");
    }
    AttributeList result = new AttributeList();
    for (String n : attrNames) {
      try {
        result.add(new Attribute(n, getAttribute(n)));
      } catch (Exception e) {
      }
    }
    return result;
  }

  public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException,
      MBeanException, ReflectionException {
    if (attribute == null) {
      throw new RuntimeOperationsException(new IllegalArgumentException("attribute must not be null"),
          "Exception occurred trying to set an attribute of a RequiredModelMBean");
    }
    String name = attribute.getName();
    Object value = attribute.getValue();
    ModelMBeanAttributeInfo ai = modelMBeanInfo.getAttribute(name);
    if (ai == null) {
      throw new AttributeNotFoundException("setAttribute failed: " + name + " is not found ");
    }
    Descriptor d = ai.getDescriptor();
    Object oldValue = null;
    try {
      oldValue = resolveGetter(name, ai);
    } catch (Exception e) {
    }
    Object setMethod = d.getFieldValue("setMethod");
    if (setMethod != null) {
      String type = ai.getType();
      invoke((String) setMethod, new Object[] {value}, new String[] {type});
    }
    d.setField("value", value);
    d.setField("lastUpdatedTimeStamp", Long.toString(System.currentTimeMillis()));
    modelMBeanInfo.setDescriptor(d, "attribute");
    if (oldValue == null ? value != null : !oldValue.equals(value)) {
      sendAttributeChangeNotification(new Attribute(name, oldValue), new Attribute(name, value));
    }
  }

  public AttributeList setAttributes(AttributeList attributes) {
    if (attributes == null) {
      throw new RuntimeOperationsException(new IllegalArgumentException("attributes must not be null"),
          "Exception occurred trying to set attributes of a RequiredModelMBean");
    }
    AttributeList result = new AttributeList();
    for (Object o : attributes) {
      Attribute a = (Attribute) o;
      try {
        setAttribute(a);
        result.add(new Attribute(a.getName(), a.getValue()));
      } catch (Exception e) {
      }
    }
    return result;
  }

  public void load() throws MBeanException, RuntimeOperationsException, InstanceNotFoundException {
  }

  public void store() throws MBeanException, RuntimeOperationsException, InstanceNotFoundException {
  }

  public void sendNotification(Notification ntfyObj) throws MBeanException, RuntimeOperationsException {
    if (ntfyObj == null) {
      throw new RuntimeOperationsException(new IllegalArgumentException("notification object must not be null"),
          "Exception occurred trying to send a notification from a RequiredModelMBean");
    }
    generic.sendNotification(ntfyObj);
  }

  public void sendNotification(String ntfyText) throws MBeanException, RuntimeOperationsException {
    if (ntfyText == null) {
      throw new RuntimeOperationsException(new IllegalArgumentException("notification message must not be null"),
          "Exception occurred trying to send a text notification from a ModelMBean");
    }
    Notification myNtfyObj = new Notification("jmx.modelmbean.generic", this, nextSequence(), ntfyText);
    sendNotification(myNtfyObj);
  }

  private synchronized long nextSequence() {
    return sequenceNumber++;
  }

  public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback)
      throws IllegalArgumentException {
    if (listener == null) {
      throw new IllegalArgumentException("notification listener must not be null");
    }
    generic.addNotificationListener(listener, filter, handback);
  }

  public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException {
    if (listener == null) {
      throw new ListenerNotFoundException("Notification listener is null");
    }
    generic.removeNotificationListener(listener);
  }

  public void removeNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback)
      throws ListenerNotFoundException {
    if (listener == null) {
      throw new ListenerNotFoundException("Notification listener is null");
    }
    generic.removeNotificationListener(listener, filter, handback);
  }

  public MBeanNotificationInfo[] getNotificationInfo() {
    List<MBeanNotificationInfo> all = new ArrayList<MBeanNotificationInfo>();
    for (MBeanNotificationInfo n : modelMBeanInfo.getNotifications()) {
      all.add(n);
    }
    boolean hasGeneric = false;
    boolean hasAttr = false;
    for (MBeanNotificationInfo n : all) {
      for (String t : n.getNotifTypes()) {
        hasGeneric |= t.equals("jmx.modelmbean.generic");
        hasAttr |= t.equals(AttributeChangeNotification.ATTRIBUTE_CHANGE);
      }
    }
    if (!hasGeneric) {
      all.add(new ModelMBeanNotificationInfo(new String[] {"jmx.modelmbean.generic"},
          "GENERIC", "A text notification has been issued by the managed resource",
          new DescriptorSupport(new String[] {"name=GENERIC", "descriptorType=notification", "severity=6", "displayName=jmx.modelmbean.generic", "log=T", "logfile=jmxmain.log"})));
    }
    if (!hasAttr) {
      all.add(new ModelMBeanNotificationInfo(new String[] {AttributeChangeNotification.ATTRIBUTE_CHANGE},
          "ATTRIBUTE_CHANGE", "Signifies that an observed MBean attribute value has changed",
          new DescriptorSupport(new String[] {"name=ATTRIBUTE_CHANGE", "descriptorType=notification", "severity=6", "displayName=jmx.attribute.change", "log=T", "logfile=jmxmain.log"})));
    }
    return all.toArray(new MBeanNotificationInfo[0]);
  }

  public void addAttributeChangeNotificationListener(NotificationListener inlistener, String inAttributeName,
      Object inhandback) throws MBeanException, RuntimeOperationsException, IllegalArgumentException {
    if (inlistener == null) {
      throw new IllegalArgumentException("Listener to be registered must not be null");
    }
    AttributeChangeNotificationFilter filter = new AttributeChangeNotificationFilter();
    MBeanAttributeInfoCheck.check(modelMBeanInfo, inAttributeName);
    if (inAttributeName == null) {
      for (javax.management.MBeanAttributeInfo ai : modelMBeanInfo.getAttributes()) {
        filter.enableAttribute(ai.getName());
      }
    } else {
      filter.enableAttribute(inAttributeName);
    }
    attributeBroadcaster.addNotificationListener(inlistener, filter, inhandback);
  }

  public void removeAttributeChangeNotificationListener(NotificationListener inlistener, String inAttributeName)
      throws MBeanException, RuntimeOperationsException, ListenerNotFoundException {
    if (inlistener == null) {
      throw new ListenerNotFoundException("Notification listener is null");
    }
    attributeBroadcaster.removeNotificationListener(inlistener);
  }

  public void sendAttributeChangeNotification(AttributeChangeNotification ntfyObj) throws MBeanException,
      RuntimeOperationsException {
    if (ntfyObj == null) {
      throw new RuntimeOperationsException(new IllegalArgumentException("attribute change notification object must not be null"),
          "Exception occurred trying to send attribute change notification of a ModelMBean");
    }
    attributeBroadcaster.sendNotification(ntfyObj);
    generic.sendNotification(ntfyObj);
  }

  public void sendAttributeChangeNotification(Attribute inOldVal, Attribute inNewVal) throws MBeanException,
      RuntimeOperationsException {
    if (inOldVal == null || inNewVal == null) {
      throw new RuntimeOperationsException(new IllegalArgumentException("Attribute object must not be null"),
          "Exception occurred trying to send attribute change notification of a ModelMBean");
    }
    if (!inOldVal.getName().equals(inNewVal.getName())) {
      throw new RuntimeOperationsException(new IllegalArgumentException("Attribute names are not the same"),
          "Exception occurred trying to send attribute change notification of a ModelMBean");
    }
    Object newVal = inNewVal.getValue();
    Object oldVal = inOldVal.getValue();
    String className = "unknown";
    if (newVal != null) {
      className = newVal.getClass().getName();
    }
    if (oldVal != null) {
      className = oldVal.getClass().getName();
    }
    AttributeChangeNotification myNtfyObj = new AttributeChangeNotification(this, nextSequence(),
        System.currentTimeMillis(), "AttributeChangeDetected", inOldVal.getName(), className, inOldVal.getValue(),
        inNewVal.getValue());
    sendAttributeChangeNotification(myNtfyObj);
  }

  public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
    if (name == null) {
      throw new NullPointerException("name of RequiredModelMBean to registered is null");
    }
    this.server = server;
    return name;
  }

  public void postRegister(Boolean registrationDone) {
    registered = registrationDone.booleanValue();
  }

  public void preDeregister() throws Exception {
  }

  public void postDeregister() {
    registered = false;
    server = null;
  }

  private static final class MBeanAttributeInfoCheck {
    static void check(ModelMBeanInfo info, String attrName) throws MBeanException {
      if (attrName == null) {
        return;
      }
      if (info.getAttribute(attrName) == null) {
        throw new RuntimeOperationsException(new IllegalArgumentException("The attribute name does not exist"),
            "Exception occurred trying to add an AttributeChangeNotification listener");
      }
    }
  }
}
