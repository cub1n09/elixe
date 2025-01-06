package elixe.modules.render;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import elixe.modules.option.ModuleFloat;
import org.lwjgl.opengl.GL11;

import elixe.Elixe;
import elixe.events.OnRender2DEvent;
import elixe.events.OnTickEvent;
import elixe.modules.Module;
import elixe.modules.ModuleCategory;
import elixe.modules.ModuleManager;
import elixe.modules.option.ModuleBoolean;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;

public class HUD extends Module {

	private ModuleManager moduleManager;
	private ArrayList<Module> modules = new ArrayList<Module>();

	public HUD() {
		super("HUD", ModuleCategory.RENDER);
		
		moduleOptions.add(watermarkOption);
		moduleOptions.add(moduleListOption);
		moduleOptions.add(moduleSizeHudOption);

		moduleOptions.add(sprintingOption);
		//moduleOptions.add(sprintingPosOption);
		moduleOptions.add(sprintingSizeOption);
	}

	boolean watermark;
	ModuleBoolean watermarkOption = new ModuleBoolean("watermark", false) {
		public void valueChanged() {
			watermark = (boolean) this.getValue();
		}
	};

	boolean moduleList;
	ModuleBoolean moduleListOption = new ModuleBoolean("module list", false) {
		public void valueChanged() {
			moduleList = (boolean) this.getValue();
		}
	};

	float moduleSizeHud=1f;
	ModuleFloat moduleSizeHudOption = new ModuleFloat("Size Hud", moduleSizeHud, 0f , 2f) {
		public void valueChanged() { moduleSizeHud = (float) this.getValue(); }
	};

	boolean sprinting;
	ModuleBoolean sprintingOption = new ModuleBoolean("sprinting", false) {
		public void valueChanged() {
			sprinting = (boolean) this.getValue();
		}
	};

	float sprintingSize = 1f;
	ModuleFloat sprintingSizeOption = new ModuleFloat("sprinting size", sprintingSize, 0f , 2f) {
		public void valueChanged() { sprintingSize = (float) this.getValue(); }
	};

	public void setModuleManager(ModuleManager moduleManager) {
		this.moduleManager = moduleManager;
		modules = (ArrayList<Module>) moduleManager.getModules().clone();
		Collections.sort(modules);
	}

	String address = "";
	@EventHandler
	private Listener<OnRender2DEvent> onRender2DEvent = new Listener<>(e -> {
		if (!this.mc.gameSettings.showDebugInfo) {
			int initialY = sprinting ? 12 : 5;
			int ySpacing = (int) (initialY * moduleSizeHud);

			if (watermark) {
				GL11.glPushMatrix();
				GL11.glScalef(moduleSizeHud, moduleSizeHud, moduleSizeHud);
				mc.fontRendererObj.drawStringWithShadow("elixe (" + Elixe.INSTANCE.build + ")", 7f / moduleSizeHud, ySpacing / moduleSizeHud, 1f, 1f);
				GL11.glPopMatrix();

				ySpacing += (int) (10 * moduleSizeHud);
			}


			if (moduleList) {
				int yModule = ySpacing;
				for (Module module : modules) {
					if (module.isToggled()) {

						GL11.glPushMatrix();
						GL11.glScalef(moduleSizeHud, moduleSizeHud, moduleSizeHud);
						mc.fontRendererObj.drawStringWithShadow(module.getName().toLowerCase(), 12f / moduleSizeHud, yModule / moduleSizeHud, 1f, 0.5f);
						GL11.glPopMatrix();

						yModule += (int) (mc.fontRendererObj.FONT_HEIGHT * moduleSizeHud);
					}
				}
			}

			
			if (sprinting) {
				GL11.glPushMatrix();
				GL11.glScalef(sprintingSize, sprintingSize, sprintingSize);
				mc.fontRendererObj.drawStringWithShadow(mc.thePlayer.isSprinting() ? "[Sprinting (Toggled)]" : "", 1f / sprintingSize, 1f / sprintingSize, 1f, 1f);
				GL11.glPopMatrix();
			}

		}
	});
	
	//handleLoginSuccess(S02PacketLoginSuccess) : void - net.minecraft.client.network.NetHandlerLoginClient
	//L:111
	public void setRemoteAddress(String addr) {
		
		if (addr.contains("local:")) {
			address = addr;
		} else {
			address = addr.split("/")[1];
		}
	}
}
