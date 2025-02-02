package com.direwolf20.buildinggadgets.client;

import com.direwolf20.buildinggadgets.client.cache.CacheTemplateProvider;
import com.direwolf20.buildinggadgets.client.events.EventTooltip;
import com.direwolf20.buildinggadgets.client.renderer.EffectBlockTER;
import com.direwolf20.buildinggadgets.client.screen.TemplateManagerGUI;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlock;
import com.direwolf20.buildinggadgets.common.blocks.OurBlocks;
import com.direwolf20.buildinggadgets.common.containers.OurContainers;
import com.direwolf20.buildinggadgets.common.containers.TemplateManagerContainer;
import com.direwolf20.buildinggadgets.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.items.OurItems;
import com.direwolf20.buildinggadgets.common.tileentities.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.tileentities.OurTileEntities;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientProxy {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientProxy.class);
    public static final CacheTemplateProvider CACHE_TEMPLATE_PROVIDER = new CacheTemplateProvider();

    public static void clientSetup() {
        LOGGER.debug("Setting up client for {}", Reference.MODID);
        KeyBindings.init();

        BlockEntityRenderers.register(OurTileEntities.EFFECT_BLOCK_TILE_ENTITY.get(), EffectBlockTER::new);
        MenuScreens.register(OurContainers.TEMPLATE_MANAGER_CONTAINER.get(), TemplateManagerGUI::new);

        MinecraftForge.EVENT_BUS.addListener(ClientProxy::onPlayerLoggedOut);
        CACHE_TEMPLATE_PROVIDER.registerUpdateListener(((GadgetCopyPaste) OurItems.COPY_PASTE_GADGET_ITEM.get()).getRender());
    }

    @SubscribeEvent
    public static void registerConstructionBlockColorHandler(RegisterColorHandlersEvent.Block event) {
        LOGGER.debug("Registering color handlers for {}", Reference.MODID);

        event.register((state, world, pos, tintIndex) -> {
            if (world != null) {
                BlockState mimicBlock = ConstructionBlock.getActualMimicBlock(world, pos);
                if (mimicBlock == null) {
                    return -1;
                }

                try {
                    return event.getBlockColors().getColor(mimicBlock, world, pos, tintIndex);
                } catch (Exception var8) {
                    return -1;
                }
            }
            return -1;
        }, OurBlocks.CONSTRUCTION_BLOCK.get());
    }

    @SubscribeEvent
    public static void registerTooltipFactory(RegisterClientTooltipComponentFactoriesEvent event) {
        LOGGER.debug("Registering custom tooltip component factories for {}", Reference.MODID);
        event.register(EventTooltip.CopyPasteTooltipComponent.Data.class, EventTooltip.CopyPasteTooltipComponent::new);
    }

    @SubscribeEvent
    public static void registerSprites(TextureStitchEvent.Pre event) {
        LOGGER.debug("Registering pre texture stitching events for {}", Reference.MODID);

        event.addSprite(new ResourceLocation(TemplateManagerContainer.TEXTURE_LOC_SLOT_TOOL));
        event.addSprite(new ResourceLocation(TemplateManagerContainer.TEXTURE_LOC_SLOT_TEMPLATE));
    }

    public static void playSound(SoundEvent sound, float pitch) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch));
    }

    private static void onPlayerLoggedOut(PlayerLoggedOutEvent event) {
        CACHE_TEMPLATE_PROVIDER.clear();
    }

    @SubscribeEvent
    public static void bakeModels(ModelEvent.BakingCompleted event) {
        LOGGER.debug("Registering baked models for {}", Reference.MODID);

        ResourceLocation ConstrName = new ResourceLocation(Reference.MODID, "construction_block");
        TextureAtlasSprite breakPart = Minecraft.getInstance().getBlockRenderer().getBlockModel(Blocks.STONE.defaultBlockState()).getParticleIcon();
        ModelResourceLocation ConstrLocation1 = new ModelResourceLocation(ConstrName, "ambient_occlusion=false,bright=false,neighbor_brightness=false");
        ModelResourceLocation ConstrLocation1a = new ModelResourceLocation(ConstrName, "ambient_occlusion=true,bright=false,neighbor_brightness=false");
        ModelResourceLocation ConstrLocation2 = new ModelResourceLocation(ConstrName, "ambient_occlusion=false,bright=true,neighbor_brightness=false");
        ModelResourceLocation ConstrLocation2a = new ModelResourceLocation(ConstrName, "ambient_occlusion=true,bright=true,neighbor_brightness=false");
        ModelResourceLocation ConstrLocation3 = new ModelResourceLocation(ConstrName, "ambient_occlusion=false,bright=false,neighbor_brightness=true");
        ModelResourceLocation ConstrLocation3a = new ModelResourceLocation(ConstrName, "ambient_occlusion=true,bright=false,neighbor_brightness=true");
        ModelResourceLocation ConstrLocation4 = new ModelResourceLocation(ConstrName, "ambient_occlusion=false,bright=true,neighbor_brightness=true");
        ModelResourceLocation ConstrLocation4a = new ModelResourceLocation(ConstrName, "ambient_occlusion=true,bright=true,neighbor_brightness=true");

        IDynamicBakedModel bakedModelLoader = new IDynamicBakedModel() {
            @Override
            public boolean isGui3d() {
                return false;
            }

            @Override
            public boolean usesBlockLight() { //isSideLit maybe?
                return false;
            }

            @Override
            public boolean isCustomRenderer() {
                return false;
            }

            @Override
            public boolean useAmbientOcclusion() {
                return true;
            }

            @Override
            public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData modelData, RenderType type) {
                BlockState facadeState = modelData.get(ConstructionBlockTileEntity.FACADE_STATE);
                if (facadeState == null || facadeState == Blocks.AIR.defaultBlockState())
                    facadeState = OurBlocks.CONSTRUCTION_DENSE_BLOCK.get().defaultBlockState();

                BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(facadeState);
                if (type != null && !model.getRenderTypes(facadeState, rand, modelData).contains(type)) { // always render in the null layer or the block-breaking textures don't show up
                    return Collections.emptyList();
                }

                return model.getQuads(facadeState, side, rand, modelData, type);
            }

            @Override
            public TextureAtlasSprite getParticleIcon() {
                //Fixes a crash until forge does something
                return breakPart;
            }

            @Override
            public ItemOverrides getOverrides() {
                return null;
            }

            @Nonnull
            @Override
            public ModelData getModelData(@Nonnull BlockAndTintGetter world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull ModelData tileData) {
                return tileData;
            }

            @Override
            public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
                return ChunkRenderTypeSet.all();
            }
        };

        IDynamicBakedModel bakedModelLoaderAmbient = new IDynamicBakedModel() {
            @Override
            public boolean isGui3d() {
                return false;
            }

            @Override
            public boolean usesBlockLight() {
                return false;
            } // is side lit maybe?

            @Override
            public boolean isCustomRenderer() {
                return false;
            }

            @Override
            public boolean useAmbientOcclusion() {
                return true;
            }

            @Override
            public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData modelData, RenderType type) {
                BlockState facadeState = modelData.get(ConstructionBlockTileEntity.FACADE_STATE);
                if (facadeState == null || facadeState == Blocks.AIR.defaultBlockState())
                    facadeState = OurBlocks.CONSTRUCTION_DENSE_BLOCK.get().defaultBlockState();

                BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(facadeState);
                if (type != null && !model.getRenderTypes(facadeState, rand, modelData).contains(type)) { // always render in the null layer or the block-breaking textures don't show up
                    return Collections.emptyList();
                }

                return model.getQuads(facadeState, side, rand, modelData, type);
            }

            @Override
            public TextureAtlasSprite getParticleIcon() {
                //Fixes a crash until forge does something
                return breakPart;
            }

            @Override
            public ItemOverrides getOverrides() {
                return null;
            }

            @Nonnull
            @Override
            public ModelData getModelData(@Nonnull BlockAndTintGetter world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull ModelData tileData) {
                return tileData;
            }

            @Override
            public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
                return ChunkRenderTypeSet.all();
            }
        };

        event.getModels().put(ConstrLocation1, bakedModelLoader);
        event.getModels().put(ConstrLocation2, bakedModelLoader);
        event.getModels().put(ConstrLocation3, bakedModelLoader);
        event.getModels().put(ConstrLocation4, bakedModelLoader);
        event.getModels().put(ConstrLocation1a, bakedModelLoaderAmbient);
        event.getModels().put(ConstrLocation2a, bakedModelLoaderAmbient);
        event.getModels().put(ConstrLocation3a, bakedModelLoaderAmbient);
        event.getModels().put(ConstrLocation4a, bakedModelLoaderAmbient);
    }
}
