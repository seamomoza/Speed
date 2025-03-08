package io.github.speed

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

class Speed : JavaPlugin(), Listener {
    private var baseTickSpeed = 20  // 기본 tick 속도 (1초당 20틱)
    private var currentTickSpeed = baseTickSpeed

    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)
        startActionbarTask()
        startPlayerBoostTask()
        startTickRateUpdateTask()
        disableCommandFeedback()
    }

    @EventHandler
    fun onPlayerDamage(event: EntityDamageEvent) {
        val player = event.entity as? Player ?: return
        val damage = event.damage.toInt()

        currentTickSpeed += damage // 받은 데미지만큼 tick 속도를 증가
    }

    private fun startTickRateUpdateTask() {
        object : BukkitRunnable() {
            override fun run() {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tick rate $currentTickSpeed")
            }
        }.runTaskTimer(this, 0L, 20L) // 1초마다 tick rate 업데이트
    }

    private fun startPlayerBoostTask() {
        object : BukkitRunnable() {
            override fun run() {
                Bukkit.getOnlinePlayers().forEach { player ->
                    val speedMultiplier = currentTickSpeed.toDouble() / baseTickSpeed

                    // 이동 속도 증가
                    player.walkSpeed = (0.2 * speedMultiplier).coerceAtMost(1.0).toFloat()

                    // 채굴 속도 증가 (공격 속도를 조정하여 간접적으로 적용)
                    var attribute = player.getAttribute(Attribute.ATTACK_SPEED)
                    attribute?.baseValue = 4.0 * speedMultiplier
                    attribute = player.getAttribute(Attribute.BLOCK_BREAK_SPEED)
                    attribute?.baseValue = 1.0* speedMultiplier
                }
            }
        }.runTaskTimer(this, 0L, 1L) // 1초마다 적용
    }

    private fun startActionbarTask() {
        object : BukkitRunnable() {
            override fun run() {
                Bukkit.getOnlinePlayers().forEach { player ->
                    player.sendActionBar("§2현재 tick: $currentTickSpeed")
                }
            }
        }.runTaskTimer(this, 0L, 1L) // 1초마다 업데이트
    }

    private fun disableCommandFeedback() {
        Bukkit.getWorlds().forEach { world ->
            world.setGameRuleValue("sendCommandFeedback", "false")
        }
    }
}
