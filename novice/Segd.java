import java.awt.*;
import java.io.*;
import java.lang.*;
import java.nio.*;
import java.util.*;
import javax.swing.*;

import edu.mines.jtk.awt.*;
import edu.mines.jtk.dsp.*;
import edu.mines.jtk.interp.*;
import edu.mines.jtk.io.*;
import edu.mines.jtk.mosaic.*;
import edu.mines.jtk.ogl.Gl.*;
import edu.mines.jtk.util.*;
import static edu.mines.jtk.util.ArrayMath.*;

public class Segd{

  public Segd(String segdDir){
    this.segdDir = segdDir;
    _segd = new ArrayList<Segdata>(0);
    readLineSegd();
  }

  public void readLineSegd(){
    try{
    File[] segdList = (new File(segdDir)).listFiles();
    //File[] segdList = new File[1];
    //segdList[0] = new File(segdDir+"/00000001.00000293.segd");
    int nshot = segdList.length; 
    for(int i=0; i<nshot; ++i){
      System.out.println(segdList[i].getName());
      Segdata seg = readSegd(segdList[i]);
      //System.out.println("sln ="+sln+" spn ="+spn+" rpf ="+rpf+" rpl ="+rpl);
      int n1 = seg.f[0].length;
      int n2 = seg.f.length;
      Sampling s1 = new Sampling(n1, 0.001, 0.0);
      Sampling s2 = new Sampling(n2, 1.0, seg.rpf);
      //lowpass2(seg.f);
      //tpow2(seg.f);
      //gain2(seg.f);
      //plot(s1,s2,seg,"Shot "+seg.sp);
      if(!(seg.sp < 0))
        _segd.add(seg);
    }
      Collections.sort(_segd, new SegdataComp());
    }catch(IOException e){
      System.out.println(e);
    }
  }

  public Segdata readSegd(File segdFile) throws IOException{ //return tiltdata-esque
    byte[] gh = zerobyte(32); // general header
    byte[] th = zerobyte(20); // trace header
    byte[] the = zerobyte(32); // trace header extension
    byte[] csh = zerobyte(32); // channel set header
    ArrayInputStream ais = new ArrayInputStream(segdFile,ByteOrder.BIG_ENDIAN);
    ais.readBytes(gh); // general header 1
    int fn = bcd2(gh,0); // file number
    ais.readBytes(gh); // general header 2
    ais.readBytes(gh); // general header 3
    int sln, spn;
    sln = bin5(gh,3); // source line number
    spn = bin5(gh,8); // source point number
    System.out.println("fn=" + fn + ", sln=" + sln + ", spn=" + spn);
    int cns = 0; // channel set number for seismic trace
    int nct = 0; // total number of channels, including aux channels
    int ncs = 0; // number of seismic channels 
    int cn, ct, nc, ic, rln, rpn;
    for(int i=0; i<16; ++i){ // for each channel set header, ...
      ais.readBytes(csh); // read channel set header 
      cn = csh[1]; // channel set number
      ct = (csh[10]>>4)&0xf; // channel type (high 4 bits)
      nc = bcd2(csh,8); // number of channels
      if(nc>0){ // if we have channels of this type, ...
        System.out.println("cn =" + cn + " nc =" + nc + " ct =" + ct);
        if(ct==1){ // if seismic, ...
          cns = cn; // remember channel set number for seismic
          ncs = nc; // remember number of seismic channels
        }
       
      nct += nc; // count total number of channels
      }
    }
    System.out.println("nct =" + nct + " cns =" + cns + " ncs =" + ncs);
    ais.skipBytes(1024); // skip extended header
    ais.skipBytes(1024); // skip external header
    int rpf = 1;
    int rpl = 1;
    int n1 = 0; // # samples
    int n2 = ncs; // #traces 
    float[][] f = null; 
    for(int j=0; j<nct; ++j){ // for all channels (including aux channels)
      ais.readBytes(th); // trace header
      cn = th[3]; // channel set number
      ic = bcd2(th,4); // channel (trace) number
      ais.readBytes(the); // trace header extension 1
      rln = bin3(the,0); // receiver line number
      rpn = bin3(the,3); // receiver point number
      n1 = bin3(the,7); // number of samples
      //System.out.println("n1 = "+n1 + " the[7-9]: " + the[7] +" "+ the[8] +" "+the[9]); 
      //System.out.println("ic =" + ic + " rln =" + rln + " rpn =" + rpn + " n1 =" + n1);
      if(ic==1){
        rpf = rpn;
      } else if(ic == n2){
        rpl = rpn;
      }
      ais.skipBytes(6*the.length); // skip trace header extensions 2-7
      if(cn==cns){ // if seismic channel, ...
        if(f == null)
          f = new float[n2][n1];
        //System.out.println("ic =" + ic + " rln =" + rln + " rpn =" + rpn);
        ais.readFloats(f[ic-1]); // get the seismic trace
      } else{
        ais.skipBytes(4*n1); // skip the aux trace
      }
    }
    ais.close();
    f = mul(1.0e-14f,f); // scale values to approx. range [-10,10]
    return new Segdata(sln,spn,rpf,rpl,f);
  }

  public void plot(Sampling s1, Sampling s2, Segdata seg, String title){
    SimplePlot sp = new SimplePlot(SimplePlot.Origin.UPPER_LEFT);
    sp.setSize(900,900);
    sp.setVLabel("Time (s)");
    if(s2.getDelta() ==1.0)
      sp.setHLabel("Station");
    else
      sp.setHLabel("Offset (km)");
    sp.setHLimits(seg.rpf, seg.rpl);
    sp.setTitle(title);
    PixelsView pv = sp.addPixels(s1,s2,seg.f);
    pv.setPercentiles(1,99);
  }

  public void tpow2(float[][] f){
    int n1 = f[0].length;
    int n2 = f.length;
    float[][] t = rampfloat(0.0f,0.002f,0.0f,n1,n2);
    mul(t,t,t);
    mul(t,f);
  }

  public void gain2(float[][] f){
    RecursiveExponentialFilter ref = new RecursiveExponentialFilter(40.0);
    for(int m = 0; m<f.length; ++m){
      if(max(abs(f))>0.0f){
        float[][] g = mul(f,f);
        ref.apply1(g,g);
        div(f,sqrt(g),f);
      }
    }
  }

  public void lowpass2(float[][] f){
    double f3db = 25.0*0.002;
    ButterworthFilter bf = new ButterworthFilter(f3db,6,ButterworthFilter.Type.LOW_PASS);
    bf.apply1ForwardReverse(f,f);
  }

  public int bcd2(byte[] b, int k){
    return (1000*((b[k  ]>>4)&0xf)+100*(b[k  ]&0xf)+
	      10*((b[k+1]>>4)&0xf)+  1*(b[k+1]&0xf));
  }

  public int bin3(byte[] b, int k){
    byte b0 = b[k  ];
    byte b1 = b[k+1];
    byte b2 = b[k+2]; 
    return (b2 & 0xFF) | ((b1 & 0xFF) << 8) | ((b0 & 0x0F) << 16);
  }

  public int bin5(byte[] b, int k){
    byte b0 = b[k  ];
    byte b1 = b[k+1];
    byte b2 = b[k+2];
    byte b3 = b[k+3];
    byte b4 = b[k+4];
    return (int)(256.0+b0*65536.0+b1*256.0+b2+b3/256.0+b4/65536.0);
  }
  
  public String segdDir = "/gpfc/ckohnke/fc2013/segd/140/"; // Linux Lab
  //public String segdDir = "/home/colton/Documents/School/SrDesign/fc2013/segd/141/"; // Laptop

  public ArrayList<Segdata> _segd;
  
}