//package io.github.naimjeg.damagenexus.debug;
//
//import io.github.naimjeg.damagenexus.DamageNexus;
//import io.github.naimjeg.damagenexus.api.enums.DamageChannel;
//import io.github.naimjeg.damagenexus.api.event.DamageNexusRegisterEvent;
//import io.github.naimjeg.damagenexus.api.rule.DamageNexusRules;
//import net.minecraft.resources.Identifier;
//import net.neoforged.bus.api.SubscribeEvent;
//import net.neoforged.fml.common.EventBusSubscriber;
//
//@EventBusSubscriber(modid = DamageNexus.MODID)
//public final class DamageNexusApiSmokeTest {
//
//    private DamageNexusApiSmokeTest() {}
//
//    @SubscribeEvent
//    public static void onDamageNexusRegister(DamageNexusRegisterEvent event) {
//        event.registerGlobalRule(
//                DamageNexusRules.offensive(
//                                Identifier.fromNamespaceAndPath(DamageNexus.MODID, "api_smoke_fire_1")
//                        )
//                        .baseModification()
//                        .display("API Smoke Fire", "Adds +1 Fire Damage.")
//                        .always()
//                        .addBaseDamage(DamageChannel.FIRE_ID, 1.0f)
//                        .trace("API Smoke +1 Fire")
//                        .build()
//        );
//    }
//}