package net.minecraft.src;

import net.lax1dude.eaglercraft.EaglercraftRandom;



public class EntitySheep extends EntityAnimal {
	private final InventoryCrafting field_90016_e = new InventoryCrafting(new ContainerSheep(this), 2, 1);

	/**
	 * Holds the RGB table of the sheep colors - in OpenGL glColor3f values - used
	 * to render the sheep colored fleece.
	 */
	public static final float[][] fleeceColorTable = new float[][] { { 1.0F, 1.0F, 1.0F }, { 0.85F, 0.5F, 0.2F }, { 0.7F, 0.3F, 0.85F }, { 0.4F, 0.6F, 0.85F }, { 0.9F, 0.9F, 0.2F }, { 0.5F, 0.8F, 0.1F }, { 0.95F, 0.5F, 0.65F },
			{ 0.3F, 0.3F, 0.3F }, { 0.6F, 0.6F, 0.6F }, { 0.3F, 0.5F, 0.6F }, { 0.5F, 0.25F, 0.7F }, { 0.2F, 0.3F, 0.7F }, { 0.4F, 0.3F, 0.2F }, { 0.4F, 0.5F, 0.2F }, { 0.6F, 0.2F, 0.2F }, { 0.1F, 0.1F, 0.1F } };

	/**
	 * Used to control movement as well as wool regrowth. Set to 40 on
	 * handleHealthUpdate and counts down with each tick.
	 */
	private int sheepTimer;

	public EntitySheep() {
		super();
		this.setSize(0.9F, 1.3F);
		float var2 = 0.23F;
		this.field_90016_e.setInventorySlotContents(0, new ItemStack(Item.dyePowder, 1, 0));
		this.field_90016_e.setInventorySlotContents(1, new ItemStack(Item.dyePowder, 1, 0));
	}

	/**
	 * Returns true if the newer Entity AI code should be run
	 */
	protected boolean isAIEnabled() {
		return true;
	}

	protected void updateAITasks() {
		super.updateAITasks();
	}

	/**
	 * Called frequently so the entity can update its state every tick as required.
	 * For example, zombies and skeletons use this to react to sunlight and start to
	 * burn.
	 */
	public void onLivingUpdate() {
		this.sheepTimer = Math.max(0, this.sheepTimer - 1);
		super.onLivingUpdate();
	}

	public int getMaxHealth() {
		return 8;
	}

	protected void entityInit() {
		super.entityInit();
		this.dataWatcher.addObject(16, Byte.valueOf((byte) 0));
	}

	/**
	 * Drop 0-2 items of this living's type. @param par1 - Whether this entity has
	 * recently been hit by a player. @param par2 - Level of Looting used to kill
	 * this mob.
	 */
	protected void dropFewItems(boolean par1, int par2) {
		if (!this.getSheared()) {
			this.entityDropItem(new ItemStack(Block.cloth.blockID, 1, this.getFleeceColor()), 0.0F);
		}
	}

	/**
	 * Returns the item ID for the item the mob drops on death.
	 */
	protected int getDropItemId() {
		return Block.cloth.blockID;
	}

	public void handleHealthUpdate(byte par1) {
		if (par1 == 10) {
			this.sheepTimer = 40;
		} else {
			super.handleHealthUpdate(par1);
		}
	}

	public float func_70894_j(float par1) {
		return this.sheepTimer <= 0 ? 0.0F : (this.sheepTimer >= 4 && this.sheepTimer <= 36 ? 1.0F : (this.sheepTimer < 4 ? ((float) this.sheepTimer - par1) / 4.0F : -((float) (this.sheepTimer - 40) - par1) / 4.0F));
	}

	public float func_70890_k(float par1) {
		if (this.sheepTimer > 4 && this.sheepTimer <= 36) {
			float var2 = ((float) (this.sheepTimer - 4) - par1) / 32.0F;
			return ((float) Math.PI / 5F) + ((float) Math.PI * 7F / 100F) * MathHelper.sin(var2 * 28.7F);
		} else {
			return this.sheepTimer > 0 ? ((float) Math.PI / 5F) : this.rotationPitch / (180F / (float) Math.PI);
		}
	}

	/**
	 * Called when a player interacts with a mob. e.g. gets milk from a cow, gets
	 * into the saddle on a pig.
	 */
	public boolean interact(EntityPlayer par1EntityPlayer) {
		ItemStack var2 = par1EntityPlayer.inventory.getCurrentItem();

		if (var2 != null && var2.itemID == Item.shears.itemID && !this.getSheared() && !this.isChild()) {
			var2.damageItem(1, par1EntityPlayer);
			this.playSound("mob.sheep.shear", 1.0F, 1.0F);
		}

		return super.interact(par1EntityPlayer);
	}

	/**
	 * (abstract) Protected helper method to write subclass entity data to NBT.
	 */
	public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {
		super.writeEntityToNBT(par1NBTTagCompound);
		par1NBTTagCompound.setBoolean("Sheared", this.getSheared());
		par1NBTTagCompound.setByte("Color", (byte) this.getFleeceColor());
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {
		super.readEntityFromNBT(par1NBTTagCompound);
		this.setSheared(par1NBTTagCompound.getBoolean("Sheared"));
		this.setFleeceColor(par1NBTTagCompound.getByte("Color"));
	}

	/**
	 * Returns the sound this mob makes while it's alive.
	 */
	protected String getLivingSound() {
		return "mob.sheep.say";
	}

	/**
	 * Returns the sound this mob makes when it is hurt.
	 */
	protected String getHurtSound() {
		return "mob.sheep.say";
	}

	/**
	 * Returns the sound this mob makes on death.
	 */
	protected String getDeathSound() {
		return "mob.sheep.say";
	}

	/**
	 * Plays step sound at given x, y, z for the entity
	 */
	protected void playStepSound(int par1, int par2, int par3, int par4) {
		this.playSound("mob.sheep.step", 0.15F, 1.0F);
	}

	public int getFleeceColor() {
		return this.dataWatcher.getWatchableObjectByte(16) & 15;
	}

	public void setFleeceColor(int par1) {
		byte var2 = this.dataWatcher.getWatchableObjectByte(16);
		this.dataWatcher.updateObject(16, Byte.valueOf((byte) (var2 & 240 | par1 & 15)));
	}

	/**
	 * returns true if a sheeps wool has been sheared
	 */
	public boolean getSheared() {
		return (this.dataWatcher.getWatchableObjectByte(16) & 16) != 0;
	}

	/**
	 * make a sheep sheared if set to true
	 */
	public void setSheared(boolean par1) {
		byte var2 = this.dataWatcher.getWatchableObjectByte(16);

		if (par1) {
			this.dataWatcher.updateObject(16, Byte.valueOf((byte) (var2 | 16)));
		} else {
			this.dataWatcher.updateObject(16, Byte.valueOf((byte) (var2 & -17)));
		}
	}

	/**
	 * This method is called when a sheep spawns in the world to select the color of
	 * sheep fleece.
	 */
	public static int getRandomFleeceColor(EaglercraftRandom par0Random) {
		int var1 = par0Random.nextInt(100);
		return var1 < 5 ? 15 : (var1 < 10 ? 7 : (var1 < 15 ? 8 : (var1 < 18 ? 12 : (par0Random.nextInt(500) == 0 ? 6 : 0))));
	}

	public EntitySheep func_90015_b(EntityAgeable par1EntityAgeable) {
		EntitySheep var2 = (EntitySheep) par1EntityAgeable;
		EntitySheep var3 = new EntitySheep();
		var3.setWorld(worldObj);
		int var4 = this.func_90014_a(this, var2);
		var3.setFleeceColor(15 - var4);
		return var3;
	}

	/**
	 * This function applies the benefits of growing back wool and faster growing up
	 * to the acting entity. (This function is used in the AIEatGrass)
	 */
	public void eatGrassBonus() {
		this.setSheared(false);

		if (this.isChild()) {
			int var1 = this.getGrowingAge() + 1200;

			if (var1 > 0) {
				var1 = 0;
			}

			this.setGrowingAge(var1);
		}
	}

	/**
	 * Initialize this creature.
	 */
	public void initCreature() {
		this.setFleeceColor(getRandomFleeceColor(this.worldObj.rand));
	}

	private int func_90014_a(EntityAnimal par1EntityAnimal, EntityAnimal par2EntityAnimal) {
		int var3 = this.func_90013_b(par1EntityAnimal);
		int var4 = this.func_90013_b(par2EntityAnimal);
		this.field_90016_e.getStackInSlot(0).setItemDamage(var3);
		this.field_90016_e.getStackInSlot(1).setItemDamage(var4);
		ItemStack var5 = CraftingManager.getInstance().findMatchingRecipe(this.field_90016_e, ((EntitySheep) par1EntityAnimal).worldObj);
		int var6;

		if (var5 != null && var5.getItem().itemID == Item.dyePowder.itemID) {
			var6 = var5.getItemDamage();
		} else {
			var6 = this.worldObj.rand.nextBoolean() ? var3 : var4;
		}

		return var6;
	}

	private int func_90013_b(EntityAnimal par1EntityAnimal) {
		return 15 - ((EntitySheep) par1EntityAnimal).getFleeceColor();
	}

	public EntityAgeable createChild(EntityAgeable par1EntityAgeable) {
		return this.func_90015_b(par1EntityAgeable);
	}
}
