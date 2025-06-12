package elixe.modules.combat;

import java.util.ArrayList;
import java.util.Collections;

import elixe.events.OnKeyEvent;
import elixe.events.OnKeybindActionEvent;
import elixe.events.OnTickEvent;
import elixe.modules.Module;
import elixe.modules.ModuleCategory;
import elixe.modules.option.*;
import elixe.utils.misc.TimerUtils;
import elixe.utils.player.InventoryItem;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class AutoSoup extends Module {

	public AutoSoup() {
		super("AutoSoup", ModuleCategory.COMBAT);

		moduleOptions.add(healthToSoupOption);
		moduleOptions.add(drinkDelayOption);
		moduleOptions.add(dropBowlOption);
		moduleOptions.add(refillOption);
		moduleOptions.add(refillKeyOption);
		moduleOptions.add(refillDelayOption);
		moduleOptions.add(recraftOption);
		moduleOptions.add(newRecraftOption);
		moduleOptions.add(recraftableItemsOption);
		moduleOptions.add(recraftDelayOption);
		moduleOptions.add(needAttackButtonOption);
	}

	float healthToSoup;
	ModuleFloat healthToSoupOption = new ModuleFloat("health to soup", 12f, 1f, 20f) {
		public void valueChanged() {
			healthToSoup = (float) this.getValue();
		}
	};

	int drinkDelay;
	ModuleInteger drinkDelayOption = new ModuleInteger("drink delay", 100, 1, 300) {
		public void valueChanged() {
			drinkDelay = (int) this.getValue();
		}
	};

	boolean dropBowl;
	ModuleBoolean dropBowlOption = new ModuleBoolean("drop bowl", false) {
		public void valueChanged() {
			dropBowl = (boolean) this.getValue();
		}
	};

	boolean refill;
	ModuleBoolean refillOption = new ModuleBoolean("refill", false) {
		public void valueChanged() {
			refill = (boolean) this.getValue();
		}
	};

	int refillKey = 0;
	ModuleKey refillKeyOption = new ModuleKey(0) {
		public void valueChanged() {
			refillKey = (int) this.getValue();
		}
	};

	int refillDelay;
	ModuleInteger refillDelayOption = new ModuleInteger("refill delay", 100, 1, 300) {
		public void valueChanged() {
			refillDelay = (int) this.getValue();
		}
	};

	boolean recraft;
	ModuleBoolean recraftOption = new ModuleBoolean("recraft", false) {
		public void valueChanged() {
			recraft = (boolean) this.getValue();
		}
	};

	boolean newRecraft;
	ModuleBoolean newRecraftOption = new ModuleBoolean("new recraft §e(BETA)§r", false) {
		public void valueChanged() {
			newRecraft = (boolean) this.getValue();
		}
	};

	boolean[] recraftableItems;
	ModuleArrayMultiple recraftableItemsOption = new ModuleArrayMultiple("recraftable items",
			new boolean[] { true, false, false }, new String[] { "mushroom", "cocoa", "cactus" }) {
		public void valueChanged() {
			recraftableItems = (boolean[]) this.getValue();
		}
	};

	int recraftDelay;
	ModuleInteger recraftDelayOption = new ModuleInteger("recraft delay", 100, 1, 300) {
		public void valueChanged() {
			recraftDelay = (int) this.getValue();
		}
	};

	boolean needAttackButton;
	ModuleBoolean needAttackButtonOption = new ModuleBoolean("require attack button", false) {
		public void valueChanged() {
			needAttackButton = (boolean) this.getValue();
		}
	};

	Object[][] combinationItems = { { Blocks.red_mushroom, Blocks.brown_mushroom }, { Items.dye }, { Blocks.cactus } };
	Object[][] bakedCombinations = { { Blocks.red_mushroom, Blocks.brown_mushroom },
			{ Blocks.brown_mushroom, Blocks.red_mushroom }, { Items.dye }, { Blocks.cactus } };

	boolean autoSouping = false;
	int autoSoupStep = 0;

	TimerUtils.MilisecondTimer drinkTimer = new TimerUtils().new MilisecondTimer();
	TimerUtils.MilisecondTimer refillTimer = new TimerUtils().new MilisecondTimer();
	TimerUtils.MilisecondTimer recraftTimer = new TimerUtils().new MilisecondTimer();

	boolean recrafting = false;
	int recraftStep = 0;

	InventoryItem bowlRecraft;
	InventoryItem[] itemsToUse = new InventoryItem[2];

	int lastItem;
	int soupInHotbar;
	int soupInInventory;

	private void toggleRefill() {
		refill = !refill;
		refillOption.setValue(refill);
		refillOption.getButton().setValue(refill);
	}

	@EventHandler
	private Listener<OnKeyEvent> onKeyEventListener = new Listener<>(e -> {
		if (refillKeyOption.getKey() == e.getKey() && e.isPressed()) {
			toggleRefill();
		}
	});

	@EventHandler
	private Listener<OnTickEvent> onTickEvent = new Listener<>(e -> {
		if (mc.currentScreen instanceof GuiInventory) {
			if (recrafting) {
				makeRecraftStep();
			} else {
				if (refill) {
					if (refillTimer.hasTimePassed(refillDelay)) {
						soupInInventory = findSoupInInventoryInOrder(); // <<-- CHANGED HERE
						if (soupInInventory != -1 && InventoryItem.hasSpaceHotbar(mc)) {
							mc.playerController.windowClick(0, soupInInventory, 0, 1, mc.thePlayer);
							refillTimer.reset();
							return;
						}
					}
				}
				// ... rest of your recraft logic unchanged
			}
		} else if (mc.currentScreen == null) {
			recrafting = false;
			if (needAttackButton && !conditionals.isHoldingAttack()) return;
			if (autoSouping) {
				makeAutoSoupStep();
			} else {
				shouldAutoSoup(true);
			}
		}
	});

	private int findSoupInInventoryInOrder() {
		for (int slot = 9; slot < 36; slot++) {
			ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(slot).getStack();
			if (stack != null && stack.getItem() == Items.mushroom_stew) {
				return slot;
			}
		}
		return -1;
	}

	// Rest of your methods like makeRecraftStep(), makeAutoSoupStep(), shouldAutoSoup() etc.
	// REMAINS UNCHANGED - as per your request

	// ... Additional unchanged methods (makeRecraftStep, makeAutoSoupStep, shouldAutoSoup, etc.) go here ...

	boolean waitForUseItem = false;
	@EventHandler
	private Listener<OnKeybindActionEvent> onKeybindActionEvent = new Listener<>(e -> {
		if (waitForUseItem) {
			int useItem = mc.gameSettings.keyBindUseItem.getKeyCode();
			if (e.getKey() == useItem) {
				if (!mc.playerController.isHittingABlock()) {
					waitForUseItem = false;
					if (!e.isPressed()) {
						KeyBinding.onTick(useItem);
					}
					if (!dropBowl) {
						autoSoupStep++;
					}
				}
			}
		}
	});

	// Keep all remaining methods from your original code...
}
