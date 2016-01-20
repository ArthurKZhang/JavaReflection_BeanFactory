package com.arthur.reflect;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class BeanFactory {

	private Map<String, Object> beanMap = new HashMap<String, Object>();

	/**
	 * bean factory initialization.
	 * 
	 * @param xml
	 *            xml configuration document
	 */
	public void init(String xml) {
		try {
			// 1.创建读取配置文件的reader对象
			// 1. Create 'reade' object for read config.. document.
			SAXReader reader = new SAXReader();

			// 2.获取当前线程中的类装载器对象
			// 2. Get 'ClassLoader' object on current thread filed.
			ClassLoader classLoader = Thread.currentThread()
					.getContextClassLoader();

			// 3.从class目录下获取指定的xml文件
			// 3. get xml file under path
			InputStream ins = classLoader.getResourceAsStream(xml);
			Document doc = reader.read(ins);
			Element root = doc.getRootElement();
			Element foo;

			// 4.遍历xml文件当中的Bean实例
			// 4. traverse beans in xml
			for (Iterator i = root.elementIterator("bean"); i.hasNext();) {
				foo = (Element) i.next();

				// 5.针对每个一个Bean实例，获取bean的属性id和class
				// 5. get beanId and beanClass for every bean in xml.
				Attribute id = foo.attribute("id");
				Attribute cls = foo.attribute("class");

				// 6.利用Java反射机制，通过class的名称获取Class对象
				// 6. get the Object of a Class by Java Reflection technology.
				Class bean = Class.forName(cls.getText());
				// 7.获取对应class的信息
				// 7. get the information of the Class
				java.beans.BeanInfo info = java.beans.Introspector
						.getBeanInfo(bean);
				// 8.获取其属性描述
				// 8. get properties
				java.beans.PropertyDescriptor pd[] = info
						.getPropertyDescriptors();

				// 9.创建一个对象，并在接下来的代码中为对象的属性赋值
				// 9. create an Object object, set attributes. this object is
				// the one of those classes(or beans or objects) in xml
				Object obj = bean.newInstance();

				// 10.遍历该bean的property属性
				// 10. traverse each property of the bean
				for (Iterator ite = foo.elementIterator("property"); ite
						.hasNext();) {
					Element foo2 = (Element) ite.next();

					// 11.获取该property的name属性
					// 11. get property name
					Attribute name = foo2.attribute("name");
					String value = null;

					// 12.获取该property的子元素value的值
					// 12. get sub-property of this property
					for (Iterator ite1 = foo2.elementIterator("value"); ite1
							.hasNext();) {
						Element node = (Element) ite1.next();
						value = node.getText();
						break;
					}

					// 13.利用Java的反射机制调用对象的某个set方法，并将值设置进去
					// 13. (by using reflection tech again)set the value into
					// object(bean/class) which we get by reflection.
					for (int k = 0; k < pd.length; k++) {
						if (pd[k].getName().equalsIgnoreCase(name.getText())) {
							Method mSet = null;
							mSet = pd[k].getWriteMethod();
							mSet.invoke(obj, value);
						}
					}
				}

				// 14.将对象放入beanMap中，其中key为id值，value为对象
				// 14. put beans into beanMap.
				// key--id; value--object(bean/class).
				beanMap.put(id.getText(), obj);
			}
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	/**
	 * 通过bean的id获取bean的对象.
	 * 
	 * @param beanName
	 *            bean的id
	 * @return 返回对应对象
	 */
	public Object getBean(String beanName) {
		Object obj = beanMap.get(beanName);
		return obj;
	}

	/**
	 * 测试方法.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		BeanFactory factory = new BeanFactory();
		factory.init("conf/config.xml");
		//test get javaBean
		JavaBean javaBean = (JavaBean) factory.getBean("javaBean01");
		System.out.println("userName=" + javaBean.getUserName());
		System.out.println("password=" + javaBean.getPassword());
		//test get another same JavaBean but with different name~
		javaBean = (JavaBean) factory.getBean("javaBean02");
		System.out.println("userName=" + javaBean.getUserName());
		System.out.println("password=" + javaBean.getPassword());
		//test get ArthurBean
		Arthur arthurBean = (Arthur) factory.getBean("arthurBean");
		System.out.println("arthurName=" + arthurBean.getArthurName());
		System.out.println("arthurSize=" + arthurBean.getArthurSize());
	}
}
