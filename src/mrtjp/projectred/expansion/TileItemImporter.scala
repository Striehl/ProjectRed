/*
 * Copyright (c) 2014.
 * Created by MrTJP.
 * All rights reserved.
 */
package mrtjp.projectred.expansion

import codechicken.lib.render.uv.{MultiIconTransformation, UVTransformation}
import mrtjp.core.block.TInstancedBlockRender
import mrtjp.core.inventory.InvWrapper
import mrtjp.core.item.ItemKey
import mrtjp.core.render.TCubeMapRender
import mrtjp.core.world.WorldLib
import mrtjp.projectred.ProjectRedExpansion
import mrtjp.projectred.transportation.PipePayload
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.util.IIcon
import net.minecraft.world.IBlockAccess

class TileItemImporter extends TileMachine with TPressureActiveDevice
{
    override def getBlock = ProjectRedExpansion.machine2

    override def doesRotate = false
    override def doesOrient = true

    //side = out, side^1 = in
    override def canAcceptInput(item:ItemKey, side:Int) = (side^1) == this.side && !powered && storage.isEmpty
    override def canAcceptBacklog(item:ItemKey, side:Int) = side == this.side

    override def canConnectSide(side:Int) = (side&6) == (this.side&6)

    override def onActivate()
    {
        importInv()
    }

    def importInv():Boolean =
    {
        val inv = InvWrapper.getInventory(world, position.offset(side^1))
        if (inv == null) return false
        val w = InvWrapper.wrap(inv)
        w.setSlotsFromSide(side)
        val list = w.getAllItemStacks
        for ((k, v) <- list)
        {
            val stack = k.makeStack(w.extractItem(k, 1))
            if (stack.stackSize > 0)
            {
                storage.add(PipePayload(stack))
                active = true
                sendStateUpdate()
                scheduleTick(4)
                exportBuffer()
                return true
            }
            return false
        }
        false
    }
}

object RenderItemRemover extends TInstancedBlockRender with TCubeMapRender
{
    var bottom:IIcon = _
    var side1:IIcon = _
    var top1:IIcon = _
    var side2:IIcon = _
    var top2:IIcon = _

    var iconT1:UVTransformation = _
    var iconT2:UVTransformation = _

    override def getData(w:IBlockAccess, x:Int, y:Int, z:Int) =
    {
        val te = WorldLib.getTileEntity(w, x, y, z, classOf[TActiveDevice])
        if (te != null) (te.side, te.rotation, if (te.active || te.powered) iconT2 else iconT1)
        else (0, 0, iconT1)
    }

    override def getInvData = (0, 0, iconT1)

    override def getIcon(s:Int, meta:Int) = s match
    {
        case 0 => bottom
        case 1 => top1
        case _ => side1
    }

    override def registerIcons(reg:IIconRegister)
    {
        bottom = reg.registerIcon("projectred:machines/importer/bottom")
        top1 = reg.registerIcon("projectred:machines/importer/top1")
        side1 = reg.registerIcon("projectred:machines/importer/side1")
        top2 = reg.registerIcon("projectred:machines/importer/top2")
        side2 = reg.registerIcon("projectred:machines/importer/side2")

        iconT1 = new MultiIconTransformation(bottom, top1, side1, side1, side1, side1)
        iconT2 = new MultiIconTransformation(bottom, top2, side2, side2, side2, side2)
    }
}