package components;


import imgui.ImGui;
import imgui.type.ImInt;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import editor.JImGui;
import jade.GameObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public abstract class Component {
    private static int ID_COUNTER = 0;
    private int uid = -1;

    public int getUid() {
        return this.uid;
    }

    public transient GameObject gameObject = null;

    public void start() {

    }

    public void update(float dt) {

    }
    
	public void editorUpdate(float dt) {
		// TODO Auto-generated method stub
		
	}
    public void imgui() {
        try {
            Field[] fields = this.getClass().getDeclaredFields();
            for (Field field : fields) {
                boolean isTransient = Modifier.isTransient(field.getModifiers());
                if (isTransient) {
                    continue;
                }

                boolean isPrivate = Modifier.isPrivate(field.getModifiers());
                if (isPrivate) {
                    field.setAccessible(true);
                }

                Class<?> type = field.getType();
                Object value = field.get(this);
                String name = field.getName();

                if (type == int.class) {
                    int val = (int) value;
                    field.set(this, JImGui.dragInt(name, val));
                } else if (type == float.class) {
                    float val = (float) value;
                    field.set(this, JImGui.dragFloat(name, val));
                } else if (type == boolean.class) {
                    boolean val = (boolean) value;
                    if (ImGui.checkbox(name + ": ", val)) {
                        field.set(this, !val);
                    }
                } else if (type == Vector2f.class) {
                	Vector2f val = (Vector2f) value;
                    JImGui.drawVec2Control(name, val);
                } else if (type == Vector3f.class) {
                    Vector3f val = (Vector3f) value;
                    float[] imVec = { val.x, val.y, val.z };
                    if (ImGui.dragFloat3(name + ": ", imVec)) {
                        val.set(imVec[0], imVec[1], imVec[2]);
                    }
                } else if (type == Vector4f.class) {
                    Vector4f val = (Vector4f) value;
                    JImGui.colorPicker4(name, val);
                } else if (type.isEnum()) {
                	String[] enumValues = getEnumValues(type);
                	String enumType = ((Enum)value).name();
                	ImInt index = new ImInt(indexOf(enumType, enumValues));
                	if (ImGui.combo(field.getName(), index, enumValues, enumValues.length)) {
                		field.set(this, type.getEnumConstants()[index.get()]);
                	}
                }

                if (isPrivate) {
                    field.setAccessible(false);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private int indexOf(String str, String[] arr) {
		for (int i = 0; i < arr.length; i++) {
			if (str.equals(arr[i])) {
				return i;
			}
		}
		return -1;
	}

	private <T extends Enum<T>> String[] getEnumValues(Class<?> type) {
		String[] enumValues = new String[type.getEnumConstants().length];
		int i = 0;
		for (Object enumIntegerValue : type.getEnumConstants()) {
			enumValues[i] = ((Enum) enumIntegerValue).name();
			i++;
		}
		return enumValues;
	}

	public void generateId(){
        if (this.uid == -1){
            this.uid = ID_COUNTER++;
        }
    }

    public static void init(int maxId) {    
        ID_COUNTER = maxId;
    }

	public void destroy() {
		
	}


}
