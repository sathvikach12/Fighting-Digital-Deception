package io.truthchain.dao;

import io.truthchain.config.Database;
import io.truthchain.model.LedgerBlock;
import io.truthchain.util.Hashing;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Append-only hash-chain writes and independent recomputation for verification. */
public class LedgerDao {
    public void ensureGenesis(Connection c)throws SQLException{
        try(PreparedStatement p=c.prepareStatement("SELECT block_index FROM ledger_blocks WHERE block_index=0 FOR UPDATE");ResultSet r=p.executeQuery()){
            if(r.next())return;
        }
        String hash=Hashing.blockHash(0,0,0,"GENESIS","GENESIS","GENESIS");
        try(PreparedStatement p=c.prepareStatement("INSERT INTO ledger_blocks(block_index,article_id,version,block_timestamp,content_hash,previous_hash,block_hash) VALUES(0,0,0,'GENESIS','GENESIS','GENESIS',?)")){p.setString(1,hash);p.executeUpdate();}
    }
    public void append(Connection c,long articleId,int version,String contentHash)throws SQLException{
        ensureGenesis(c);
        long index;String previous;String timestamp=Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS).toString();
        try(PreparedStatement p=c.prepareStatement("SELECT block_index,block_hash FROM ledger_blocks ORDER BY block_index DESC LIMIT 1 FOR UPDATE");ResultSet r=p.executeQuery()){
            if(r.next()){index=r.getLong(1)+1;previous=r.getString(2);}else{index=0;previous="GENESIS";}
        }
        String hash=Hashing.blockHash(index,articleId,version,timestamp,contentHash,previous);
        try(PreparedStatement p=c.prepareStatement("INSERT INTO ledger_blocks(block_index,article_id,version,block_timestamp,content_hash,previous_hash,block_hash) VALUES(?,?,?,?,?,?,?)")){
            p.setLong(1,index);p.setLong(2,articleId);p.setInt(3,version);p.setString(4,timestamp);p.setString(5,contentHash);p.setString(6,previous);p.setString(7,hash);p.executeUpdate();
        }
    }
    public List<LedgerBlock> all()throws SQLException{
        String sql="SELECT b.id,b.block_index,b.article_id,b.version,b.block_timestamp,b.content_hash,b.previous_hash,b.block_hash,COALESCE(v.title,n.title,'Removed article') title FROM ledger_blocks b LEFT JOIN news n ON n.id=b.article_id LEFT JOIN news_versions v ON v.article_id=b.article_id AND v.version=b.version ORDER BY b.block_index";
        List<LedgerBlock> out=new ArrayList<>();try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()){while(r.next())out.add(new LedgerBlock(r.getLong("id"),r.getLong("block_index"),r.getLong("article_id"),r.getInt("version"),r.getString("block_timestamp"),r.getString("content_hash"),r.getString("previous_hash"),r.getString("block_hash"),r.getString("title")));}return out;
    }
    public List<String> verify()throws SQLException{
            List<LedgerBlock> blocks=all();List<String> issues=new ArrayList<>();String expectedPrevious="GENESIS";long expectedIndex=0;
        for(LedgerBlock b:blocks){
            if(b.blockIndex()!=expectedIndex)issues.add("Unexpected block index at row "+b.blockIndex());
            if(!expectedPrevious.equals(b.previousHash()))issues.add("Previous hash mismatch at block "+b.blockIndex());
            String calculated=Hashing.blockHash(b.blockIndex(),b.articleId(),b.version(),b.timestamp(),b.contentHash(),b.previousHash());
            if(!calculated.equals(b.blockHash()))issues.add("Block hash mismatch at block "+b.blockIndex());
            expectedPrevious=b.blockHash();expectedIndex++;
        }
        if(blocks.isEmpty())issues.add("Ledger has no genesis block");
        else if(blocks.get(0).articleId()!=0||blocks.get(0).version()!=0||!"GENESIS".equals(blocks.get(0).previousHash())||!"GENESIS".equals(blocks.get(0).contentHash())||!"GENESIS".equals(blocks.get(0).timestamp()))issues.add("Genesis block fields are invalid");
        else if(!Hashing.blockHash(0,0,0,"GENESIS","GENESIS","GENESIS").equals(blocks.get(0).blockHash()))issues.add("Genesis block hash is invalid");
        return issues;
    }
    public boolean verifyArticle(long articleId)throws SQLException{
        String sql="SELECT n.category_id,n.title,n.description,n.publisher,n.published_at,n.is_deleted,v.category_id vc,v.title vt,v.description vd,v.publisher vp,v.published_at vdate,v.is_deleted vdel,b.content_hash FROM ledger_blocks b LEFT JOIN news n ON n.id=b.article_id LEFT JOIN news_versions v ON v.article_id=b.article_id AND v.version=b.version WHERE b.article_id=? AND b.block_index>0 ORDER BY b.version DESC LIMIT 1";
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setLong(1,articleId);try(ResultSet r=p.executeQuery()){if(!r.next())return false;
            boolean versionExists=r.getObject("vc")!=null;
            if(!versionExists&&r.getObject("category_id")==null)return false;
            long category=versionExists?r.getLong("vc"):r.getLong("category_id");String title=versionExists?r.getString("vt"):r.getString("title");String description=versionExists?r.getString("vd"):r.getString("description");String publisher=versionExists?r.getString("vp"):r.getString("publisher");String date=versionExists?r.getString("vdate"):r.getString("published_at");boolean deleted=versionExists?r.getBoolean("vdel"):r.getBoolean("is_deleted");
            String actual=deleted?"DELETED":io.truthchain.service.ArticleText.hash(category,title,description,publisher,date);return actual.equals(r.getString("content_hash"));}}
    }
}
