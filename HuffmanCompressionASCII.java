package huffmancompressionascii;
import java.util.*;
import java.io.*;
/**
 *
 * @author Daanish Mahajan
 */
/*Assumptions
1->There are atleast 2 distinct characters in the text
*/
public class HuffmanCompressionASCII {
    static File TextFile,EncodeFile,DecodeFile,FreqFile;
    static final int BlockSize=256,BitLength=8;
    public static class Node{
         Node left,right;
         char value='\0';//default value
         int freq;
         Node(char ch,int count){value=ch;freq=count;}
         Node(int count,Node L,Node R){freq=count;left=L;right=R;}
    }
      
    public static class MakeHuffmanTree{
      MakeHuffmanTree(int freq[]){
       Make(freq);
      }
      //Using min heap 
      PriorityQueue<Node> HuffmanTree=new PriorityQueue<>((N1,N2)->N1.freq-N2.freq);
      //root node of Huffman Tree
      Node root;
      //make huffman tree
        public void Make(int freq[]){
         //adding all non zero frequent characters in the heap
         for(int i=0;i<BlockSize;i++){
           if(freq[i]!=0)HuffmanTree.add(new Node((char)i,freq[i]));
         }
         
         while(HuffmanTree.size()!=1){
          //pop 2 least frequent nodes
          Node N1=HuffmanTree.poll(),N2=HuffmanTree.poll();
          //add a new node with the frequency=sum of frequency of 2 nodes and node N1 as left child and N2 as right child
          HuffmanTree.add(new Node(N1.freq+N2.freq,N1,N2));
         }
         //the node with the maximum frequency is the root node
         root=HuffmanTree.poll();
        }
    }
    
    public static class Encode{
        File file,tofile,freqfile;
        StringBuilder s=new StringBuilder();
        Encode(File toencode,File Target,File freq)throws IOException{
         file=toencode;tofile=Target;freqfile=freq;
         
         Read_InputFromTextFile();
         
         GetCode();
         
         WriteFrequencyArray();
         
         Encode_InputFromTextFile();
        }
        //considering char from ASCII value 0-65535
        int freq[]=new int[BlockSize];
        String code[]=new String[BlockSize];
        int TotalBits=0;
        //read input
        public void Read_InputFromTextFile() throws IOException{
            BufferedReader br=new BufferedReader(new FileReader(file));
            int ch;
            while((ch=br.read())!=-1){
             freq[ch]++;
            }
        }
        //getting codes for different characters from HuffmanTree
        public void GetCode(){
         MakeHuffmanTree HuffmanTree=new MakeHuffmanTree(freq); 
         
         GetCode(HuffmanTree.root,new StringBuilder());
        }
        //dfs traversal of Huffman Tree
        public void GetCode(Node N,StringBuilder sb){
         //root node
         if(N.left==null&&N.right==null){
          code[N.value]=sb.toString();
          TotalBits+=freq[N.value]*code[N.value].length();
          return;
         }
         if(N.left!=null){
          GetCode(N.left,sb.append((char)0));
          sb.deleteCharAt(sb.length()-1);
         }
         if(N.right!=null){
          GetCode(N.right,sb.append((char)1));
          sb.deleteCharAt(sb.length()-1);
         }
        }
        //writing frequency array to text file
        public void WriteFrequencyArray()throws IOException{
         PrintWriter pw=new PrintWriter(new FileWriter(freqfile));
         for(int i=0;i<BlockSize;i++){
          if(freq[i]!=0)pw.println(i+" "+freq[i]);
         }pw.print(TotalBits);
         pw.flush();pw.close();
        }
        //encoding the input file
        public void Encode_InputFromTextFile()throws IOException{
         BufferedReader br=new BufferedReader(new FileReader(file));
         OutputStream outputstream=new FileOutputStream(tofile);
         int bitcount=0;
            byte write=0;
            int ch;boolean start=true;
            while((ch=br.read())!=-1){
             for(int i=0;i<code[ch].length();i++){
              char c=code[ch].charAt(i);
              if(TotalBits<BitLength){
               outputstream.write((byte)c);
               continue;
              }
              if(start){
               write|=c;start=false;
              }
              else{
               write<<=1;
               write|=c;
              }
              bitcount++;
              if(bitcount==BitLength){
               outputstream.write(write);
               write='\0';
               start=true;
               TotalBits-=BitLength;
               bitcount=0;
              }
             }
            }
            outputstream.flush();
            outputstream.close();
        }
    }
    public static class Decode{
     File freqfile,encode,decode;
     Decode(File in,File out,File freq)throws IOException{
      encode=in;decode=out;freqfile=freq;
      
      ReadFrequencyArray();
      
      GetCode();
      
      Decode_EncodedFile();
     }
     int freq[]=new int[BlockSize];
     int TotalBits;
     Node root;
     //reading frequency array from file
     public void ReadFrequencyArray()throws IOException{
      BufferedReader br=new BufferedReader(new FileReader(freqfile));
      String s[];
      while((s=br.readLine().trim().split("\\s+")).length!=1){
       freq[Integer.parseInt(s[0])]=Integer.parseInt(s[1]);
      }
       TotalBits=Integer.parseInt(s[0]);
     }
     //Making HuffmanTree
     public void GetCode(){
      MakeHuffmanTree HuffmanTree=new MakeHuffmanTree(freq); 
      root=HuffmanTree.root;
     }
     //Decoding the Encoded File
     public void Decode_EncodedFile()throws IOException{
      InputStream inputstream =new FileInputStream(encode);
      PrintWriter pw=new PrintWriter(new FileWriter(decode));
      int ch;
      Node node=root;
      while((ch=inputstream.read())!=-1){
       if(TotalBits<BitLength){
        if((char)ch==0){
         node=node.left;
        }else{
         node=node.right;
        }
        if(node.value!='\0'){
         pw.print((char)node.value);
         node=root;
        }
        continue;
       }
       for(int i=(BitLength-1);i>=0;i--){
        int c=(ch>>i)&1;
        if(c==0){
         node=node.left;
        }else{
         node=node.right;
        }
        if(node.value!='\0'){
          pw.print((char)node.value);
          node=root;
         }
       }TotalBits-=BitLength;
      }
      pw.flush();pw.close();
     }
    }
    public static void Run()throws IOException{
      long StartTime,EndTime;double f=(int)(1E9);
      
      StartTime=System.nanoTime();
      Encode encoder=new Encode(TextFile,EncodeFile,FreqFile);
      EndTime=System.nanoTime();
      System.out.println("Encode Time-> "+(EndTime-StartTime)/f+" sec");
      
      StartTime=System.nanoTime();
      Decode decoder=new Decode(EncodeFile,DecodeFile,FreqFile);
      EndTime=System.nanoTime();
      System.out.println("Decode Time-> "+(EndTime-StartTime)/f+" sec");
    }
    public static void main(String[] args) throws IOException{
      BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
      String FileName[]=new String[4];
      for(int i=0;i<4;i++)FileName[i]=br.readLine().trim();
      TextFile=new File(FileName[0]);
      EncodeFile=new File(FileName[1]);
      DecodeFile=new File(FileName[2]);
      FreqFile=new File(FileName[3]);
      Run();
    }
    /*
    G:\\coding\\project\\Files\\small_text.txt
    G:\\coding\\project\\Files\\upd_small_EncodeFile.txt
    G:\\coding\\project\\Files\\small_DecodeFile.txt
    G:\\coding\\project\\Files\\upd_small_FreqFile.txt
    */
}
