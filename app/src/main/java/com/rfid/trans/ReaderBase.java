package com.rfid.trans;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.security.InvalidParameterException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.rfid.hf.MyService;
import com.rfid.hf.Util;
import com.rfid.serialport.SerialPort;
import android.os.SystemClock;
import android.util.Log;

public class ReaderBase {
	private SerialPort mSerialPort = null;
	private InputStream mInStream = null;
	private OutputStream mOutStream = null;
    private byte[]recvBuff=new byte[50000];
    private int[] recvLength= new int[1];
    private byte ComAddr=(byte)255;
    private int RecvTimeOut=0;
    private int logswitch=0;
    private void getCRC(byte[] data,int Len)
	 {
		 try
		 {
			 int i, j;
				int current_crc_value = 0xFFFF;
				for (i = 0; i <Len ; i++)
				{
				    current_crc_value = current_crc_value ^ (data[i] & 0xFF);
				    for (j = 0; j < 8; j++)
				    {
				        if ((current_crc_value & 0x01) != 0)
				            current_crc_value = (current_crc_value >> 1) ^ 0x8408;
				        else
				            current_crc_value = (current_crc_value >> 1);
				    }
				}
				data[i++] = (byte) (current_crc_value & 0xFF);
				data[i] = (byte) ((current_crc_value >> 8) & 0xFF); 
		 }
		 catch(Exception e)
		 {
			 ;
		 }
	 }
    private boolean CheckCRC(byte[] data,int len)
	 {
		 try
		 {
			 byte[]daw =new byte[256];
			 System.arraycopy(data, 0, daw, 0, len);
			 getCRC(daw,len);
			 if(0==daw[len+1] && 0==daw[len])
			 {
				 return true;
			 }
			 else
			 {
				 return false;
			 }
		 }
		 catch(Exception e)
		 {
			 return false;
		 }
	 }
		 
	private int SendCMD(byte[]CMD)
	{
		MyService.setTime(System.currentTimeMillis());
		recvLength[0]=0;
		try {
			byte[]buffer=new byte[4096];
			mInStream.read(buffer);
			mOutStream.write(CMD);
			if(logswitch==1)Log.d("Send", Util.bytesToHexString(CMD,0,(CMD[0]&255)+1));
			return 0;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0x30;
		}
	}
	
	private int GetCMDData(byte[]data,int[]Nlen,long endTime)
	{
		byte[]buffer=new byte[2560];
		int Count=0;
		byte[]btArray = new byte[20000];
		int btLength=0;
		long beginTime = System.currentTimeMillis();
		try{
			while((System.currentTimeMillis()-beginTime)<endTime)
			{
				try {
					Count=0;
					SystemClock.sleep(5);
					Count = mInStream.read(buffer);
					MyService.setTime(System.currentTimeMillis());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(Count>0)
				{
					System.arraycopy(buffer, 0, btArray, btLength, Count);
					btLength+=Count;
					if((((btArray[0]&255)+1)==btLength)&&((btArray[0]&255)>3))
					{
						if(CheckCRC(btArray,btLength))
						{
							if(logswitch==1)Log.d("Recv", Util.bytesToHexString(btArray,0,(btArray[0]&255)+1));
							System.arraycopy(btArray, 0, data, 0, btLength);
							Nlen[0]=btLength;
							return 0;
						}
					}
				}
			}
		}catch(Exception e)
		{e.toString();}
		return 0x30;
	}
	
	public int ConnectReader(String serialpath,int speed,int logswitch)
	{
		try {
			mSerialPort = new SerialPort(new File(serialpath), speed, logswitch);
			this.logswitch = logswitch;
		} catch (SecurityException e) {
			;
		} catch (IOException e) {
			;
		} catch (InvalidParameterException e) {
			;
		}
		if(mSerialPort!=null)
		{
			mInStream = mSerialPort.getInputStream();
			mOutStream = mSerialPort.getOutputStream();
			SystemClock.sleep(150);
			return 0;
		}
		else
		{
			return 0x30;
		}	
	}
	
	public void DisconnectReader()
	{
		if(mSerialPort!=null)
		{
			try {
				if(mInStream!=null)
				{
					mInStream.close();
					mInStream=null;
				}
				if(mOutStream!=null)
				{
					mOutStream.close();
					mOutStream=null;
				}
				mSerialPort.close();
				mSerialPort=null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public int GetReaderInfo(byte[] TVersionInfo,byte[]RFU,byte[]ReaderType,byte[]TrType,byte[]ScanTime)
	{
		byte[]buffer = new byte[6];
		buffer[0]=(byte)(0x05);
		buffer[1]=(byte)ComAddr;
		buffer[2]=(byte)(0x00);
		buffer[3]=(byte)(0xF0);
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,1000);
		if(result==0)
		{
			ComAddr = recvBuff[1];
			if(recvBuff[2]==0)
			{
				TVersionInfo[0] = recvBuff[3];
				TVersionInfo[1] = recvBuff[4];
				RFU[0] = recvBuff[5];
				RFU[1] = recvBuff[6];
				ReaderType[0] = recvBuff[7];
				TrType[0] = recvBuff[8];
				TrType[1] = recvBuff[9];
				ScanTime[0] = recvBuff[10];
				RecvTimeOut = (ScanTime[0]&255)*100;
			}
			return recvBuff[2] &255;
		}
		return 0x30;
	}
	
	public int OpenRf()
	{
		byte[]buffer = new byte[6];
		buffer[0]=(byte)(0x05);
		buffer[1]=(byte)ComAddr;
		buffer[2]=(byte)(0x02);
		buffer[3]=(byte)(0xF0);
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,1000);
		if(result==0)
		{
			MyService.SetRfStatus(true);
			return recvBuff[2] &255;
		}
		return 0x30;
	}
	
	public int CloseRf()
	{
		byte[]buffer = new byte[6];
		buffer[0]=(byte)(0x05);
		buffer[1]=(byte)ComAddr;
		buffer[2]=(byte)(0x01);
		buffer[3]=(byte)(0xF0);
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,1000);
		if(result==0)
		{
			MyService.SetRfStatus(false);
			return recvBuff[2] &255;
		}
		return 0x30;
	}
	
	public int SetActiveANT(byte Antenna)
	{
		byte[]buffer = new byte[7];
		buffer[0]=(byte)(0x06);
		buffer[1]=(byte)ComAddr;
		buffer[2]=(byte)(0x08);
		buffer[3]=(byte)(0xF0);
		buffer[4]=Antenna;
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,1000);
		if(result==0)
		{
			return recvBuff[2] &255;
		}
		return 0x30;
	}
	
	public int GetActiveANT(byte[] Antenna)
	{
		byte[]buffer = new byte[6];
		buffer[0]=(byte)(0x05);
		buffer[1]=(byte)ComAddr;
		buffer[2]=(byte)(0x09);
		buffer[3]=(byte)(0xF0);
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,1000);
		if(result==0)
		{
			if(recvBuff[2]==0)
			{
				Antenna[0] =recvBuff[3];
			}
			return recvBuff[2] &255;
		}
		return 0x30;
	}
	
	public int GetPower(byte[] Power)
	{
		byte[]buffer = new byte[6];
		buffer[0]=(byte)(0x05);
		buffer[1]=(byte)ComAddr;
		buffer[2]=(byte)(0x22);
		buffer[3]=(byte)(0xF0);
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,1000);
		if(result==0)
		{
			if(recvBuff[2]==0)
			{
				Power[0] =(byte)(recvBuff[4]&0x7F);
			}
			return recvBuff[2] &255;
		}
		return 0x30;
	}
	public int SetPower(byte Power)
	{
		byte[]buffer = new byte[7];
		buffer[0]=(byte)(0x06);
		buffer[1]=(byte)ComAddr;
		buffer[2]=(byte)(0x21);
		buffer[3]=(byte)(0xF0);
		buffer[4]=(byte)Power;
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,1000);
		if(result==0)
		{
			return recvBuff[2] &255;
		}
		return 0x30;
	}
	

	
	public int ResetToReady(byte state,byte[]uid,byte[] Errorcode)
	{
		byte[]buffer = new byte[14];
		if(state==0)
		{
			buffer[0]=(byte)(0x0D);
			buffer[1]=(byte)ComAddr;
			buffer[2]=(byte)(0x26);
			buffer[3]=state;
			System.arraycopy(uid, 0, buffer, 4, 8);
		}
		else
		{
			buffer[0]=(byte)(0x05);
			buffer[1]=(byte)ComAddr;
			buffer[2]=(byte)(0x26);
			buffer[3]=state;
		}
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,1000);
		if(result==0)
		{
			if(recvBuff[2]==0)
			{
				Errorcode[0] = 0;
			}
			else
			{
				Errorcode[0] = recvBuff[3];
			}
			return recvBuff[2] &255;
		}
		return 0x30;
	}


	public int ReadSingleBlock(byte state,byte UID[],byte blockNumber,
							   byte SecurityWord[],byte Data[],byte ErrorCode[])
	{
		byte[]buffer = new byte[15];
		buffer[0]=(byte)(0x0E);
		buffer[1]=(byte)ComAddr;
		buffer[2]=(byte)(0x20);
		buffer[3]=state;
		System.arraycopy(UID, 0, buffer, 4, 8);
		buffer[12]=blockNumber;
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,1000);
		if(result==0)
		{
			if(recvBuff[2]==0)
			{
				SecurityWord[0] = recvBuff[3];
				System.arraycopy(recvBuff,4,Data,0,recvBuff[0]-5);
				ErrorCode[0] = 0;
			}
			else if(recvBuff[2]==0x0F)
			{
				ErrorCode[0] = recvBuff[3];
			}
			return recvBuff[2] &255;
		}
		return 0x30;
	}

	public int WriteSingleBlock(byte state,byte UID[],byte blockNumber,
								byte Data[],byte ErrorCode[])
	{
		byte[]buffer = new byte[30];
		if(state==0x00 || state==0x08)//4字节
		{
			buffer = new byte[19];
			buffer[0]=(byte)(0x12);
			buffer[1]=(byte)ComAddr;
			buffer[2]=(byte)(0x21);
			buffer[3]=state;
			System.arraycopy(UID, 0, buffer, 4, 8);
			buffer[12]=blockNumber;
			System.arraycopy(Data, 0, buffer, 13, 4);
		}
		else if(state==0x04 || state==0x0C)//8字节
		{
			buffer = new byte[23];
			buffer[0]=(byte)(0x16);
			buffer[1]=(byte)ComAddr;
			buffer[2]=(byte)(0x21);
			buffer[3]=state;
			System.arraycopy(UID, 0, buffer, 4, 8);
			buffer[12]=blockNumber;
			System.arraycopy(Data, 0, buffer, 13, 8);
		}



		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,1000);
		if(result==0)
		{
			if(recvBuff[2]==0)
			{
				ErrorCode[0] = 0;
			}
			else if(recvBuff[2]==0x0F)
			{
				ErrorCode[0] = recvBuff[3];
			}
			return recvBuff[2] &255;
		}
		return 0x30;
	}


	public int ReadMultipleBlock(byte state,byte UID[],byte startblockNumber,
								 byte numberOfBlock,byte SecurityWord[],byte Data[],byte ErrorCode[])
	{
		byte[]buffer = new byte[16];
		buffer[0]=(byte)(0x0F);
		buffer[1]=(byte)ComAddr;
		buffer[2]=(byte)(0x23);
		buffer[3]=state;
		System.arraycopy(UID, 0, buffer, 4, 8);
		buffer[12]=startblockNumber;
		buffer[13]=numberOfBlock;
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,3000);
		if(result==0)
		{
			if(recvBuff[2]==0)
			{
				int datalen= recvBuff[0]&255 - 4;
				if(datalen/numberOfBlock == 5)//4字节
				{
					for(int m=0;m<numberOfBlock;m++)
					{
						SecurityWord[m] = recvBuff[3+5*m];
						System.arraycopy(recvBuff,4+5*m,Data,4*m,4);
					}
					ErrorCode[0] = 0;
				}
				else
				{
					for(int m=0;m<numberOfBlock;m++)
					{
						SecurityWord[m] = recvBuff[3+9*m];
						System.arraycopy(recvBuff,4+9*m,Data,8*m,8);
					}
					ErrorCode[0] = 0;
				}


			}
			else if(recvBuff[2]==0x0F)
			{
				ErrorCode[0] = recvBuff[3];
			}
			return recvBuff[2] &255;
		}
		return 0x30;
	}
	
	private int GetInventoryData(byte[] pdata, int[]size,boolean MultiInventoty)
	{
		int read_len=0;
		int RealLen=0;
		byte[] Buff=new byte[256];
		byte[]recvBuff=new byte[20000];
		int index=0;
	    long time1 = System.currentTimeMillis();
		long time2=0;
		do
		{
            int len=0;
			try {
				SystemClock.sleep(1);
				len = mInStream.read(Buff);
				MyService.setTime(System.currentTimeMillis());
                if(logswitch==1)
                	Log.d("Recv", Util.bytesToHexString(Buff,0,len));
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(len>0)	//读取到字节数据
			{
                System.arraycopy(Buff, 0, recvBuff, read_len, len);
				read_len=read_len+len;
				while((read_len-index)>=14)
				{
					if((recvBuff[index]&255) == 0x0D)
					{
						byte[] arr_crc=new byte[20];
						System.arraycopy(recvBuff, index, arr_crc, 0, 14);
						if(CheckCRC(arr_crc,14))
						{
							time1 = System.currentTimeMillis();
							System.arraycopy(arr_crc, 0, pdata, RealLen, 14);
							index+=14;
							RealLen+=14;
						}
						else
						{
							index++;
						}

					}
					else
					{
						index++;
					}
				}
				if((!MultiInventoty)&&(RealLen==14))
				{
					size[0] =RealLen;
					return 0;
				}
				byte[] arr_crc=new byte[10];
				if(read_len>=5)
				{
					System.arraycopy(recvBuff, read_len-5, arr_crc, 0, 5);
				}
				if(CheckCRC(arr_crc,5)&&(arr_crc[0]==0x04))
				{
					System.arraycopy(arr_crc, 0, pdata, RealLen, 5);
					RealLen+=5;
					size[0] =RealLen;
					return 0;
				}
			}
			time2 = System.currentTimeMillis();
		}while(time2 - time1 <= RecvTimeOut*3+8000);//最大询查时间
		return ( 0x30 );
	}
	
	private void DeleteRepeatUID(byte[]uidold,int numold,byte[]uidnew,int[] numnew)
	{
	    Map<String, Integer> IndexMap =new LinkedHashMap<String, Integer>();
	    numnew[0]=0;
		for(int i=0;i<numold;i++)
		{
			String temp = "";
			String str="";
			StringBuffer bf = new StringBuffer("");
			for(int k=0;k<9;k++)
			{
				str = Integer.toHexString(uidold[i*9+k] & 0xff);
				if(str.length() == 1){
	    			bf.append("0");
	    		}
	    		bf.append(str);
			}
			temp = bf.toString().toUpperCase();
			Integer findIndex = IndexMap.get(temp);
			if (findIndex == null)//找到不重复的UID号
			{
				IndexMap.put(temp,IndexMap.size());
				for(int n=0;n<9;n++)
				{
					uidnew[numnew[0]*9+n] = uidold[i*9+n];
				}
				numnew[0]++;
			}
		}
	}
	public int Inventory(byte state,byte AFI,byte[]Data,int[]tagnum)
	{
        int cardnum=0;
        int result,nLen=0;
        byte[] Buff=new byte[20000];
        byte[] UIDBuff=new byte[20000];
        boolean MultiInventoty=false;
        int turnCount=0;
        int CardIndex=0;
        byte[]buffer;
        if ((state == 0x01) || (state == 0x03) || (state == 0x07))
        {
            buffer = new byte[7];
            buffer[0]=0x06;
            buffer[1]=(byte)ComAddr;
            buffer[2]=0x01;
            buffer[3]=state;
            buffer[4]=AFI;
        }
        else
        {
            buffer = new byte[6];
            buffer[0]=0x05;
            buffer[1]=(byte)ComAddr;
            buffer[2]=0x01;
            buffer[3]=state;
        }

        getCRC(buffer,buffer[0]-1);
        if((state==0x00)||(state==0x01))
        {
            MultiInventoty=false;
        }
        else
        {
            MultiInventoty=true;
        }
        SendCMD(buffer);
        result = GetInventoryData(recvBuff, recvLength,MultiInventoty);
        nLen=recvLength[0];
        if (result == 0)
        {
            if(nLen==5)//无卡
            {
                return ( recvBuff[2] );
            }
            else if(nLen==14)//单张查询
            {
                System.arraycopy(recvBuff, 0, Buff, 0, 14);
                if (CheckCRC(Buff, 14))
                {
                    System.arraycopy(Buff, 3, Data, 0, 9);
                    tagnum[0] = 1;
                    return ( Buff[2] );
                }
                else
                {
                    return ( 0x31 );
                }
            }
            else
            {
                do
                {
                    System.arraycopy(recvBuff, 14*CardIndex, Buff, 0, 14);
                    if (CheckCRC(Buff, 14))
                    {
                        System.arraycopy(Buff, 3, UIDBuff, turnCount*9, 9);
                        turnCount=turnCount+1;
                        CardIndex++;
                    }
                    else
                    {
                        CardIndex++;
                    }
                    nLen=nLen-14;
                }while(nLen-14>=5);
                cardnum=turnCount;
                DeleteRepeatUID(UIDBuff,cardnum,Data,tagnum);
                return ( recvBuff[recvLength[0]-3]);
            }
        }
        else
        {
            return ( result );
        }
	}


	public int LockBlock(byte state,byte[] UID,byte blockNumber,byte ErrorCode[])
	{
		byte[]buffer = new byte[30];
		buffer = new byte[15];
		buffer[0]=(byte)(0x0E);
		buffer[1]=(byte)ComAddr;
		buffer[2]=(byte)(0x22);
		buffer[3]=state;
		System.arraycopy(UID, 0, buffer, 4, 8);
		buffer[12]=blockNumber;
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,1000);
		if(result==0)
		{
			if(recvBuff[2]==0)
			{
				ErrorCode[0] = 0;
			}
			else if(recvBuff[2]==0x0F)
			{
				ErrorCode[0] = recvBuff[3];
			}
			return recvBuff[2] &255;
		}
		return 0x30;
	}


	public int WriteAFI(byte state,byte[] UID,byte AFI,byte ErrorCode[])
	{
		byte[]buffer = new byte[30];
		buffer = new byte[15];
		buffer[0]=(byte)(0x0E);
		buffer[1]=(byte)ComAddr;
		buffer[2]=(byte)(0x27);
		buffer[3]=state;
		System.arraycopy(UID, 0, buffer, 4, 8);
		buffer[12]=AFI;
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,1000);
		if(result==0)
		{
			if(recvBuff[2]==0)
			{
				ErrorCode[0] = 0;
			}
			else if(recvBuff[2]==0x0F)
			{
				ErrorCode[0] = recvBuff[3];
			}
			return recvBuff[2] &255;
		}
		return 0x30;
	}

	public int LockAFI(byte state,byte[] UID,byte ErrorCode[])
	{
		byte[]buffer = new byte[30];
		buffer = new byte[14];
		buffer[0]=(byte)(0x0D);
		buffer[1]=(byte)ComAddr;
		buffer[2]=(byte)(0x28);
		buffer[3]=state;
		System.arraycopy(UID, 0, buffer, 4, 8);
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,1000);
		if(result==0)
		{
			if(recvBuff[2]==0)
			{
				ErrorCode[0] = 0;
			}
			else if(recvBuff[2]==0x0F)
			{
				ErrorCode[0] = recvBuff[3];
			}
			return recvBuff[2] &255;
		}
		return 0x30;
	}

	public int WriteDSFID(byte state,byte[] UID,byte DSFID,byte ErrorCode[])
	{
		byte[]buffer = new byte[30];
		buffer = new byte[15];
		buffer[0]=(byte)(0x0E);
		buffer[1]=(byte)ComAddr;
		buffer[2]=(byte)(0x29);
		buffer[3]=state;
		System.arraycopy(UID, 0, buffer, 4, 8);
		buffer[12]=DSFID;
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,1000);
		if(result==0)
		{
			if(recvBuff[2]==0)
			{
				ErrorCode[0] = 0;
			}
			else if(recvBuff[2]==0x0F)
			{
				ErrorCode[0] = recvBuff[3];
			}
			return recvBuff[2] &255;
		}
		return 0x30;
	}

	public int LockDSFID(byte state,byte[] UID,byte ErrorCode[])
	{
		byte[]buffer = new byte[30];
		buffer = new byte[14];
		buffer[0]=(byte)(0x0D);
		buffer[1]=(byte)ComAddr;
		buffer[2]=(byte)(0x2A);
		buffer[3]=state;
		System.arraycopy(UID, 0, buffer, 4, 8);
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,1000);
		if(result==0)
		{
			if(recvBuff[2]==0)
			{
				ErrorCode[0] = 0;
			}
			else if(recvBuff[2]==0x0F)
			{
				ErrorCode[0] = recvBuff[3];
			}
			return recvBuff[2] &255;
		}
		return 0x30;
	}

	public int GetSystemInformation(byte state,byte[] UID,byte[]InformationFlag,byte[]DSFID,byte[]AFI,byte[]MemorySize,byte ErrorCode[])
	{
		byte[]buffer = new byte[30];
		buffer = new byte[14];
		buffer[0]=(byte)(0x0D);
		buffer[1]=(byte)ComAddr;
		buffer[2]=(byte)(0x2B);
		buffer[3]=state;
		System.arraycopy(UID, 0, buffer, 4, 8);
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,1000);
		if(result==0)
		{
			if(recvBuff[2]==0)
			{
				ErrorCode[0] = 0;
				int CNT=3;
				InformationFlag[0] = recvBuff[CNT];
				CNT++;
				CNT = CNT + 8;//去除UID
				if((InformationFlag[0] & 0x01)==0x01)
				{
					DSFID[0] = recvBuff[CNT];
					CNT++;
				}
				if((InformationFlag[0] & 0x02)==0x02)
				{
					AFI[0] = recvBuff[CNT];
					CNT ++;
				}
				if((InformationFlag[0] & 0x04)==0x04)
				{
					MemorySize[0] = recvBuff[CNT];
					CNT ++;
					MemorySize[1] = recvBuff[CNT];
					CNT ++;
				}
			}
			else if(recvBuff[2]==0x0F)
			{
				ErrorCode[0] = recvBuff[3];
			}
			return recvBuff[2] &255;
		}
		return 0x30;
	}


	public int TransparentRead(int RspLength,int CustomDatalength,byte[] CustomData,int[] FeedbackDataLength,byte[] FeedbackData,byte[]ErrorCode)
	{
		byte[]buffer = new byte[30];
		buffer = new byte[0x07+ CustomDatalength];
		buffer[0]=(byte)(0x06+ CustomDatalength);
		buffer[1]=(byte)ComAddr;
		buffer[2]=(byte)(0x02);
		buffer[3]=(byte)0xE0;
		buffer[4]=(byte)RspLength;
		System.arraycopy(CustomData, 0, buffer, 5, CustomDatalength);
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,1000);
		if(result==0)
		{
			FeedbackDataLength[0]=0;
			ErrorCode[0] = 0;
			if(recvBuff[2]==0)
			{
				FeedbackDataLength[0] =(recvBuff[0]&255) - 4;
				if(FeedbackDataLength[0]>0)
				{
					System.arraycopy(recvBuff,3,FeedbackData,0,FeedbackDataLength[0]);
				}
			}
			else if(recvBuff[2]==0x0F)
			{
				ErrorCode[0] = recvBuff[3];
			}
			return recvBuff[2] &255;
		}
		return 0x30;
	}


	public int TransparentWrite(byte[]option,int RspLength,int CustomDatalength,byte[] CustomData,int[] FeedbackDataLength,byte[] FeedbackData,byte[]ErrorCode)
	{
		byte[]buffer = new byte[30];
		buffer = new byte[0x0B+ CustomDatalength];
		buffer[0]=(byte)(0x0A+ CustomDatalength);
		buffer[1]=(byte)ComAddr;
		buffer[2]=(byte)(0x03);
		buffer[3]=(byte)0xE0;
		System.arraycopy(option, 0, buffer, 4, 4);
		buffer[8]=(byte)RspLength;
		System.arraycopy(CustomData, 0, buffer, 9, CustomDatalength);
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,1000);
		if(result==0)
		{
			FeedbackDataLength[0]=0;
			ErrorCode[0] = 0;
			if(recvBuff[2]==0)
			{
				FeedbackDataLength[0] =(recvBuff[0]&255) - 4;
				if(FeedbackDataLength[0]>0)
				{
					System.arraycopy(recvBuff,3,FeedbackData,0,FeedbackDataLength[0]);
				}
			}
			else if(recvBuff[2]==0x0F)
			{
				ErrorCode[0] = recvBuff[3];
			}
			return recvBuff[2] &255;
		}
		return 0x30;
	}

	public int EASLogin(byte[]UID)
	{
		byte[]cmdData=new byte[100];
		byte[]respData=new byte[100];
		int respLength=0;
		int cmdLength=0;
		int[] feedBackDataLength=new int[1];
		byte[]errorCode=new byte[1];
		byte[] pwd = new byte[4];
		pwd[0]=pwd[1]=pwd[2]=pwd[3]=0;
		respLength=0x03;
		cmdLength=15;
		cmdData[0]=(byte)0x23;
		cmdData[1]=(byte)0xE4;
		cmdData[2]=(byte)0x16;
		System.arraycopy(UID,0,cmdData,3,8);
		System.arraycopy(pwd,0,cmdData,11,4);
		int fCmdRet=TransparentRead( respLength, cmdLength, cmdData, feedBackDataLength, respData, errorCode);
		if(fCmdRet==0)
			return 0;
		else if(fCmdRet!=0x30)
		{
			return 1;
		}
		else
			return 0x30;

	}
	public int SetEAS(byte State,byte[]UID)
	{
		byte[]option=new byte[4];
		byte[]cmdData=new byte[100];
		byte[]respData=new byte[100];
		int respLength=0;
		int cmdLength=0;
		int[] feedBackDataLength=new int[1];
		byte[]errorCode=new byte[1];
		int nflag = ((UID[4]&255) & 0x18)>>3;
		if(nflag ==0 )
		{
			option[0]=(byte)0x00;
			option[1]=(byte)0x44;
			option[2]=(byte)0x00;
			option[3]=(byte)0x00;
		}
		else
		{
			option[0]=(byte)0x01;
			option[1]=(byte)0x00;
			option[2]=(byte)0x00;
			option[3]=(byte)0x96;
		}
		if(State==0)
		{

			respLength=0x03;
			cmdLength=0x03;
			cmdData[0]=(byte)0x03;
			cmdData[1]=(byte)0xA2;
			cmdData[2]=UID[6];
		}
		else if(State==1)
		{
			if(UID[6]==0x16)
			{
				int fcmdret = EASLogin(UID);
				if(fcmdret!=0)
				{
					return fcmdret;
				}
			}
			respLength=0x03;
			cmdLength=0x0B;
			if(nflag==0)
				cmdData[0]=0x23;
			else
				cmdData[0]=0x63;
			cmdData[1]=(byte)0xA2;
			cmdData[2]=UID[6];
			System.arraycopy(UID,0,cmdData,3,8);
		}
		else
		{
			return 0xFF;
		}
		int fCmdRet=TransparentWrite( option, respLength, cmdLength, cmdData, feedBackDataLength, respData, errorCode);
		if(fCmdRet==0)
			return 0;
		else if(fCmdRet!=0x30)
		{
			return 1;
		}
		else
			return 0x30;
	}


	public int ResetEAS(byte State,byte[]UID)
	{
		byte[]option=new byte[4];
		byte[]cmdData=new byte[100];
		byte[]respData=new byte[100];
		int respLength=0;
		int cmdLength=0;
		int[] feedBackDataLength=new int[1];
		byte[]errorCode=new byte[1];
		int nflag = ((UID[4]&255) & 0x18)>>3;
		if(nflag ==0 )
		{
			option[0]=(byte)0x00;
			option[1]=(byte)0x44;
			option[2]=(byte)0x00;
			option[3]=(byte)0x00;
		}
		else
		{
			option[0]=(byte)0x01;
			option[1]=(byte)0x00;
			option[2]=(byte)0x00;
			option[3]=(byte)0x96;
		}
		if(State==0)
		{

			respLength=0x03;
			cmdLength=0x03;
			cmdData[0]=(byte)0x03;
			cmdData[1]=(byte)0xA3;
			cmdData[2]=UID[6];
		}
		else if(State==1)
		{
			if(UID[6]==0x16)
			{
				int fcmdret = EASLogin(UID);
				if(fcmdret!=0)
				{
					return fcmdret;
				}
			}
			respLength=0x03;
			cmdLength=0x0B;
			if(nflag==0)
				cmdData[0]=0x23;
			else
				cmdData[0]=0x63;
			cmdData[1]=(byte)0xA3;
			cmdData[2]=UID[6];
			System.arraycopy(UID,0,cmdData,3,8);
		}
		else
		{
			return 0xFF;
		}
		int fCmdRet=TransparentWrite( option, respLength, cmdLength, cmdData, feedBackDataLength, respData, errorCode);
		if(fCmdRet==0)
			return 0;
		else if(fCmdRet!=0x30)
		{
			return 1;
		}
		else
			return 0x30;
	}

	public int LockEAS(byte State,byte[]UID)
	{
		byte[]option=new byte[4];
		byte[]cmdData=new byte[100];
		byte[]respData=new byte[100];
		int respLength=0;
		int cmdLength=0;
		int[] feedBackDataLength=new int[1];
		byte[]errorCode=new byte[1];
		int nflag = ((UID[4]&255) & 0x18)>>3;
		if(nflag ==0 )
		{
			option[0]=(byte)0x00;
			option[1]=(byte)0x44;
			option[2]=(byte)0x00;
			option[3]=(byte)0x00;
		}
		else
		{
			option[0]=(byte)0x01;
			option[1]=(byte)0x00;
			option[2]=(byte)0x00;
			option[3]=(byte)0x96;
		}
		if(State==0)
		{

			respLength=0x03;
			cmdLength=0x03;
			cmdData[0]=(byte)0x03;
			cmdData[1]=(byte)0xA4;
			cmdData[2]=UID[6];
		}
		else if(State==1)
		{
			if(UID[6]==0x16)
			{
				int fcmdret = EASLogin(UID);
				if(fcmdret!=0)
				{
					return fcmdret;
				}
			}
			respLength=0x03;
			cmdLength=0x0B;
			if(nflag==0)
				cmdData[0]=0x23;
			else
				cmdData[0]=0x63;
			cmdData[1]=(byte)0xA4;
			cmdData[2]=UID[6];
			System.arraycopy(UID,0,cmdData,3,8);
		}
		else
		{
			return 0xFF;
		}
		int fCmdRet=TransparentWrite( option, respLength, cmdLength, cmdData, feedBackDataLength, respData, errorCode);
		if(fCmdRet==0)
			return 0;
		else if(fCmdRet!=0x30)
		{
			return 1;
		}
		else
			return 0x30;
	}

	public int EASAlarm(byte State,byte[]UID)
	{
		byte[]option=new byte[4];
		byte[]cmdData=new byte[100];
		byte[]respData=new byte[100];
		int respLength=0;
		int cmdLength=0;
		int[] feedBackDataLength=new int[1];
		byte[]errorCode=new byte[1];
		if(State==0)
		{
			respLength=(byte)0x23;
			cmdLength=(byte)0x03;
			cmdData[0]=(byte)0x03;
			cmdData[1]=(byte)0xA5;
			cmdData[2]=UID[6];
		}
		else if(State==1)
		{
			respLength=(byte)0x23;
			cmdLength=(byte)0x0B;
			cmdData[0]=(byte)0x23;
			cmdData[1]=(byte)0xA5;
			cmdData[2]=UID[6];
			System.arraycopy(UID,0,cmdData,3,8);
		}
		else
		{
			return 0xFF;
		}
		int fCmdRet=TransparentRead(respLength, cmdLength, cmdData, feedBackDataLength, respData, errorCode);
		if(fCmdRet==0)
			return 0;
		else if(fCmdRet!=0x30)
		{
			return 1;
		}
		else
			return 0x30;
	}

	public int GetRandomNum(byte[]UID,byte[]RandomNum)
	{
		byte[]cmdData=new byte[100];
		byte[]respData=new byte[100];
		int respLength=0;
		int cmdLength=0;
		int[] feedBackDataLength=new int[1];
		byte[]errorCode=new byte[1];
		respLength=(byte)0x05;
		cmdLength=(byte)0x0B;
		cmdData[0]=(byte)0x23;
		cmdData[1]=(byte)0xB2;
		cmdData[2]=UID[6];
		System.arraycopy(UID,0,cmdData,3,8);
		int fCmdRet=TransparentRead(respLength, cmdLength, cmdData, feedBackDataLength, respData, errorCode);
		if(fCmdRet==0)
		{
			if(errorCode[0]==0 && feedBackDataLength[0]==2)
			{
				RandomNum[0] = respData[0];
				RandomNum[1] = respData[1];
				return 0;
			}
			return 1;
		}
		else if(fCmdRet!=0x30)
		{
			return 1;
		}
		else
			return 0x30;
	}


	public int SetPassWord(byte[]UID,byte[]PassWord)
	{
		byte[] RandomNum = new byte[2];
		int fCmdRet = GetRandomNum(UID,RandomNum);
		if(fCmdRet==0)
		{
			byte[]option=new byte[4];
			byte[]cmdData=new byte[100];
			byte[]respData=new byte[100];
			int respLength=0;
			int[] feedBackDataLength=new int[1];
			byte[]errorCode=new byte[1];
			int cmdLength=0;
			respLength=0x03;
			cmdLength=0x10;
			cmdData[0]=(byte)0x23;
			cmdData[1]=(byte)0xB3;
			cmdData[2]=UID[6];
			System.arraycopy(UID,0,cmdData,3,8);
			cmdData[11]=(byte)0x10;
			cmdData[12]=(byte)(PassWord[0]^RandomNum[0]);
			cmdData[13]=(byte)(PassWord[1]^RandomNum[1]);
			cmdData[14]=(byte)(PassWord[2]^RandomNum[0]);
			cmdData[15]=(byte)(PassWord[3]^RandomNum[1]);
			fCmdRet=TransparentRead(respLength, cmdLength, cmdData, feedBackDataLength, respData, errorCode);
			return fCmdRet;
		}
		return fCmdRet;
	}

	public int WritePassWord(byte[]UID,byte[]PassWord)
	{
		byte[]option=new byte[4];
		byte[]cmdData=new byte[100];
		byte[]respData=new byte[100];
		int respLength=0;
		int[] feedBackDataLength=new int[1];
		byte[]errorCode=new byte[1];
		int cmdLength=0;
		int nflag = ((UID[4]&255) & 0x18)>>3;
		if(nflag ==0 )
		{
			option[0]=(byte)0x00;
			option[1]=(byte)0x44;
			option[2]=(byte)0x00;
			option[3]=(byte)0x00;
			cmdData[0]=(byte)0x23;
		}
		else
		{
			option[0]=(byte)0x01;
			option[1]=(byte)0x00;
			option[2]=(byte)0x00;
			option[3]=(byte)0x96;
			cmdData[0]=(byte)0x63;
		}
		respLength=0x03;
		cmdLength=0x10;
		cmdData[1]=(byte)0xB4;
		cmdData[2]=UID[6];
		System.arraycopy(UID,0,cmdData,3,8);
		cmdData[11]=(byte)0x10;
		cmdData[12]=PassWord[0];
		cmdData[13]=PassWord[1];
		cmdData[14]=PassWord[2];
		cmdData[15]=PassWord[3];
		int fCmdRet=TransparentWrite(option,respLength, cmdLength, cmdData, feedBackDataLength, respData, errorCode);
		return fCmdRet;
	}

	public int LockPassWord(byte[]UID)
	{
		byte[]option=new byte[4];
		byte[]cmdData=new byte[100];
		byte[]respData=new byte[100];
		int respLength=0;
		int[] feedBackDataLength=new int[1];
		byte[]errorCode=new byte[1];
		int cmdLength=0;
		int nflag = ((UID[4]&255) & 0x18)>>3;
		if(nflag ==0 )
		{
			option[0]=(byte)0x00;
			option[1]=(byte)0x44;
			option[2]=(byte)0x00;
			option[3]=(byte)0x00;
			cmdData[0]=(byte)0x23;
		}
		else
		{
			option[0]=(byte)0x01;
			option[1]=(byte)0x00;
			option[2]=(byte)0x00;
			option[3]=(byte)0x96;
			cmdData[0]=(byte)0x63;
		}
		respLength=0x03;
		cmdLength=0x0C;
		cmdData[1]=(byte)0xB5;
		cmdData[2]=UID[6];
		System.arraycopy(UID,0,cmdData,3,8);
		cmdData[11]=(byte)0x10;
		int fCmdRet=TransparentWrite(option,respLength, cmdLength, cmdData, feedBackDataLength, respData, errorCode);
		return fCmdRet;
	}

	public int PassWordProtect(byte[]UID,byte optionFlag)
	{
		byte[]option=new byte[4];
		byte[]cmdData=new byte[100];
		byte[]respData=new byte[100];
		int respLength=0;
		int[] feedBackDataLength=new int[1];
		byte[]errorCode=new byte[1];
		int cmdLength=0;
		option[0]=(byte)0x00;
		option[1]=(byte)0x44;
		option[2]=(byte)0x00;
		option[3]=(byte)0x00;
		respLength=0x03;
		cmdLength=0x0B;
		if(optionFlag==0)
		cmdData[0]=(byte)0x63;
		else
            cmdData[0]=(byte)0x23;
		cmdData[1]=(byte)0xA6;
		cmdData[2]=UID[6];
		System.arraycopy(UID,0,cmdData,3,8);
		int fCmdRet=TransparentWrite(option,respLength, cmdLength, cmdData, feedBackDataLength, respData, errorCode);
        /*if(fCmdRet==0x0E)
        {
            fCmdRet=CloseRf();
            fCmdRet=OpenRf();
            byte state =0;
            byte[] InformationFlag= new byte[1];
            byte[] DSFID= new byte[1];
            byte[] AFI= new byte[1];
            byte[] MemorySize = new byte[2];
            byte[] ICReference= new byte[1];
            fCmdRet = GetSystemInformation(state,UID,InformationFlag,DSFID,AFI,MemorySize,errorCode);
            if(fCmdRet==0)
            {
                state=0;
                fCmdRet = WriteAFI(state,UID,AFI[0],errorCode);
                if((fCmdRet ==0x0F)&&(errorCode[0]==0x0F))
                {
                    return SetPassWord(UID,PassWord);
                }
            }
        }*/
        return fCmdRet;
	}

}

