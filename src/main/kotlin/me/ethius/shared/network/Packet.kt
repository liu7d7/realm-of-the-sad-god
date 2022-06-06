package me.ethius.shared.network

import me.ethius.shared.int
import me.ethius.shared.string

open class Packet(val id:int, vararg val data:string) {

    constructor(id:int, vararg data:Any) : this(id, *data.map { it.toString() }.toTypedArray())

    override fun toString():string {
        return "#$id;${data.joinToString(";")}".replace("\n", "%n")
    }

    companion object {


        /**
         * C2S
         *
         *     0 id:long
         *     1 profileToml:string
         */
        const val _id_logon = 0

        /**
         * C2S/S2C
         *
         *     -1 no data
         */
        const val _id_logoff = -1

        /**
         * C2S
         *
         *     0 x:double
         *     1 y:double
         *
         * S2C
         *
         *     0 id:long
         *     1 x:double
         *     2 y:double
         */
        const val _id_move = 1

        /**
         * C2S/S2C
         *
         *     0 message:string
         *
         */
        const val _id_chat = 2

        /**
         * C2S/S2C
         *
         *     0 texDataId:string
         *     1 x:double
         *     2 y:double
         */
        const val _id_particle = 3

        /**
         * S2C
         *
         *     0 x:int
         *     1 y:int
         *     2 texDataId:string
         *     3 envId:string
         */
        const val _id_block_info = 4

        /**
         * C2S
         *
         *     0 positionList:string
         */
        const val _id_block_info_request = -4

        /**
         * C2S
         *
         *     0 worldName:string
         */
        const val _id_world_request = 5

        /**
         * S2C
         *
         *     0 worldName:string
         */
        const val _id_world_info = 6

        /**
         * C2S/S2C
         *
         *     0 entityId:long
         *     1 className|worldName:string
         *     2 x:double
         *     3 y:double
         *     4..N data:Any
         */
        const val _id_spawn_entity = 7

        /**
         * S2C
         *
         *     0 entityId:long
         */
        const val _id_delete_entity = -7

        /**
         * S2C
         *
         *     0 amount:int
         */
        const val _id_exp_add = 8

        /**
         * S2C
         *
         *     0 x:double
         *     1 y:double
         *     2 items:string
         */
        const val _id_bag_spawn = 9

        /**
         * C2S
         *
         *     0 damageSourceId:long
         *     1 entityId:long
         *     2 damage:double
         */
        const val _id_damage_entity = 10

        /**
         * C2S
         *
         *     0 hp:double
         *
         * S2C
         *
         *     0 entityId:long
         *     1 hp:double
         */
        const val _id_hp_update = 11

        /**
         * S2C
         *
         *     0 data:string
         */
        const val _id_block_info_batch = 12

        /**
         * C2S/S2C
         *
         *     0 entityId:long
         *     1 effectAsString:string
         *
         * Contract
         *
         * C2S:
         *
         *     After receiving on server side, will sync with client side and
         *     add to appropriate entity, and will be broadcast to all other
         *     clients in world.
         *
         * S2C:
         *
         *     After receiving on client side, will accept no matter what and
         *     add to appropriate entity.
         *
         */
        const val _id_effect_add = 13

        /**
         * S2C
         *
         *     0 entityId:long
         *     1 displayString:string
         *     2 displayColor:long
         */
        const val _id_entity_notification = 14

        fun fromString(str:string):Packet {
            if (!str.startsWith("#")) throw IllegalArgumentException("Invalid packet string: $str")
            val str = str.drop(1)
            val split = str.split(";")
            val id = split[0].toInt()
            val data = split.drop(1).map { it.replace("%n", "\n") }.toTypedArray()
            return Packet(id, *data)
        }
    }

}