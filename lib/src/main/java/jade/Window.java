package jade;

import org.lwjgl.opengl.GL;

import renderer.*;
import scenes.LevelEditorScene;
import scenes.LevelScene;
import scenes.Scene;
import util.AssetPool;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;


import org.lwjgl.glfw.GLFWErrorCallback;

public class Window {
    private int width, height;
    private String title;
    private long glfwWindow;
    private ImGuiLayer imguiLayer;
    private Framebuffer framebuffer; 
    private PickingTexture pickingTexture;

    public float r, g, b, a;

    private static Window window = null;

    private static Scene currentScene;

    private Window() {
        this.width = 1920;
        this.height = 1080;
        this.title = "Mario";
        r = 1;
        g = 1;
        b = 1;
        a = 1;
    }

    public static void changeScene(int newScene) {
        switch (newScene) {
            case 0:
                currentScene = new LevelEditorScene();
                break;
            case 1:
                currentScene = new LevelScene();
                break;
            default:
                assert false : "Unknown scene '" + newScene +"'";
                break;
        }
        currentScene.load();
        currentScene.init();
        currentScene.start();
    }

    public static Window get() {
        if (Window.window == null){
            Window.window = new Window();
        }

        return Window.window;
    }

    public static Scene getScene() {
        get();
        return Window.currentScene;
    }
    public void run() {
        // System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        init();
        loop();

        //Free the memory
        glfwFreeCallbacks(glfwWindow);
        glfwDestroyWindow(glfwWindow);

        //Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();


    }

    public void init() {
        //Setup an error callback
        GLFWErrorCallback.createPrint(System.err).set();

        //Initialize GLFW
        if (!glfwInit()){
            throw new IllegalStateException("Unable to initialize GLFW.");
        }

        //Configure GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);

        //Create the window
        glfwWindow = glfwCreateWindow(this.width, this.height, this.title, NULL, NULL );
        if (glfwWindow == NULL){
            throw new IllegalStateException("Failed to create the GLFW window.");
        }

        glfwSetCursorPosCallback(glfwWindow, MouseListener::mousePosCallBack);
        glfwSetMouseButtonCallback(glfwWindow, MouseListener::mouseButtonCallBack);
        glfwSetScrollCallback(glfwWindow, MouseListener::mouseScrollCallBack);
        glfwSetKeyCallback(glfwWindow, KeyListener::keyCallBack);
        glfwSetWindowSizeCallback(glfwWindow, (w, newWidth, newHeight) -> {
            Window.setWidth(newWidth);
            Window.setHeight(newHeight);
        });

        //Make the OpenGL context current
        glfwMakeContextCurrent(glfwWindow);
        //Enable v-sync
        glfwSwapInterval(1);

        //make the window visible
        glfwShowWindow(glfwWindow);

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        this.imguiLayer = new ImGuiLayer(glfwWindow);
        this.imguiLayer.initImGui();
        
        this.framebuffer = new Framebuffer(3840, 1080);
        this.pickingTexture = new PickingTexture(3840, 1080);
        
        glViewport(0, 0, 3840, 1080);

        Window.changeScene(0);

    }
    public void loop() {
        float beginTime = (float)glfwGetTime();
        float endTime;
        float dt = -1.0f;


        Shader defaultShader = AssetPool.getShader("/media/anthony/Enterprise/projects/portfolioGame/lib/assets/shaders/default.glsl");
        Shader pickingShader = AssetPool.getShader("/media/anthony/Enterprise/projects/portfolioGame/lib/assets/shaders/pickingShader.glsl");
        while (!glfwWindowShouldClose(glfwWindow)) {
            //Poll events
            glfwPollEvents();
            
            // Render pass 1. Render to picking texture
            glDisable(GL_BLEND);
            pickingTexture.enableWriting();
            
            glViewport(0, 0, 3840, 1080);
            glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            

            Renderer.bindShader(pickingShader);
            currentScene.render();
            
            if (MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_LEFT)) {
            	int x = (int)MouseListener.getScreenX();
            	int y = (int)MouseListener.getScreenY();
            	System.out.println(pickingTexture.readPixel(x, y));
            }
            
            pickingTexture.disableWriting();
            glEnable(GL_BLEND);
            
            // Render pass 2. Render actual game

            DebugDraw.beginFrame();
            this.framebuffer.bind();
            glClearColor(r, g,b,a);
            glClear(GL_COLOR_BUFFER_BIT);
            
            

            if (dt >=0){
                DebugDraw.draw();
                Renderer.bindShader(defaultShader); 
                currentScene.update(dt);
                currentScene.render();
            }
            
            this.framebuffer.unbind();
            this.imguiLayer.update(dt, currentScene);
            glfwSwapBuffers(glfwWindow);

            endTime = (float) glfwGetTime();
            dt = endTime -beginTime;
            beginTime = endTime;

        }
        currentScene.saveExit();
    }

    public static int getWidth() {
        return get().width;
    }

    public static int getHeight() {
        return get().height;
    }

    public static void setWidth(int newWidth) {
        get().width = newWidth;
    }

    public static void setHeight(int newHeight) {
        get().height = newHeight;        
    }

    public static Framebuffer getFramebuffer() {
    	return get().framebuffer;
    }
    
    public static float getTargetAspectRatio() {
    	return 16.0f/9.0f;
    }

}



