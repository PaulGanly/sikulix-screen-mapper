package com.paulganly.sikuli.mapper.ui;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;

public class ScreenShotProcessor implements ClipboardOwner {

	public BufferedImage getImageFromClipboard() {
		Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
		if(transferable != null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor)){
			try{
				return (BufferedImage) transferable.getTransferData(DataFlavor.imageFlavor);
			} catch(Exception e){
				e.printStackTrace();
			}
		}
		return null;
	}

	public void setImageToClipboard(BufferedImage image) {
		Transferable transferable = new TransferableImage(image);
		Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
		c.setContents(transferable, this);
	}

	@Override
	public void lostOwnership(Clipboard arg0, Transferable arg1) {
		System.out.println("Lost Clipboard Ownership");
	}
}
