package editor;

import org.joml.Vector2f;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiWindowFlags;
import jade.MouseListener;
import jade.Window;

public class GameViewWindow {
	
	private float leftX, rightX, topY, bottomY;

	public void imgui() {
		ImGui.begin("Game Viewport", ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse);

		ImVec2 windowSize = getLargestSizeForViewport();
		ImVec2 windowPos = getCeneteredPositionForViewport(windowSize);
		

		ImGui.setCursorPos(windowPos.x, windowPos.y);

		ImVec2 topLeft = new ImVec2();
		ImGui.getCursorScreenPos(topLeft);
		topLeft.x -= ImGui.getScrollX();
		topLeft.y -= ImGui.getScrollY();
		leftX = topLeft.x;
		bottomY = topLeft.y;
		rightX = topLeft.x + windowSize.x;
		topY = topLeft.y + windowSize.y;

		int textureId = Window.getFramebuffer().getTextureId();
		ImGui.image(textureId, windowSize.x, windowSize.y, 0, 1,1,0);

		MouseListener.setGameViewportPos(new Vector2f(topLeft.x, topLeft.y));
		MouseListener.setGameViewportSize(new Vector2f(windowSize.x, windowSize.y));

		ImGui.end();
	}

	private ImVec2 getLargestSizeForViewport() {
		ImVec2 windowSize = new ImVec2();
		ImGui.getContentRegionAvail(windowSize);
		windowSize.x -= ImGui.getScrollX();
		windowSize.y -= ImGui.getScrollY();

		float aspectWidth = windowSize.x;
		float aspectHeight = aspectWidth / Window.getTargetAspectRatio();
		if (aspectHeight > windowSize.y) {
			//we must switch to pillarbox mode
			aspectHeight = windowSize.y;
			aspectWidth = aspectHeight * Window.getTargetAspectRatio();
		}

		return new ImVec2(aspectWidth, aspectHeight);

	}

	private ImVec2 getCeneteredPositionForViewport(ImVec2 aspectSize) {
		ImVec2 windowSize = new ImVec2();
		ImGui.getContentRegionAvail(windowSize);
		windowSize.x -= ImGui.getScrollX();
		windowSize.y -= ImGui.getScrollY();

		float viewportX = (windowSize.x / 2.0f)- (aspectSize.x / 2.0f);
		float viewportY = (windowSize.y / 2.0f)- (aspectSize.y / 2.0f);

		return new ImVec2(viewportX + ImGui.getCursorPosX(), viewportY + ImGui.getCursorPosY());
	}

	public boolean getWantCaptureMouse() {
		return MouseListener.getX() >= leftX && MouseListener.getX() <= rightX && 
				MouseListener.getY() >= bottomY && MouseListener.getY() <= topY;
	}
}
