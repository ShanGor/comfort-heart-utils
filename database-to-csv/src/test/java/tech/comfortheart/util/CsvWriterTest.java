package tech.comfortheart.util;

import org.junit.Test;
import org.mockito.stubbing.OngoingStubbing;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import static org.mockito.Mockito.*;

/**
 * Unit test for simple App.
 */
public class CsvWriterTest
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() throws IOException, SQLException {
        String[] headers = {"Hey", "You"};
        List<String[]> data = new LinkedList<>();

        data.add(new String[]{"are you okay", "no, i don't know"});
        data.add(new String[]{"not sure about this", "could you help?"});
        for (int i=0; i<1000; i++) {
            data.add(new String[]{"hey " + i, "you " + i});
        }

        String filePath = "/tmp/test.csv";
        CsvWriter csvWriter = new CsvWriter(filePath, headers);
        try  {
            csvWriter.writeRecords(data);

            System.out.println("CSV文件创建成功,文件路径:"+filePath);
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            csvWriter.close();
        }

        new File(filePath).delete();

        /**
         * Test with ResultSet
         */
        ResultSet rs = mock(ResultSet.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when( metaData.getColumnCount() ).thenReturn(9);
        when(metaData.getColumnType(1)).thenReturn(Types.VARCHAR);
        when(metaData.getColumnType(2)).thenReturn(Types.VARCHAR);
        when(metaData.getColumnType(3)).thenReturn(Types.VARCHAR);
        when(metaData.getColumnType(4)).thenReturn(Types.BOOLEAN);
        when(metaData.getColumnType(5)).thenReturn(Types.FLOAT);
        when(metaData.getColumnType(6)).thenReturn(Types.INTEGER);
        when(metaData.getColumnType(7)).thenReturn(Types.DOUBLE);
        when(metaData.getColumnType(8)).thenReturn(Types.DECIMAL);
        when(metaData.getColumnType(9)).thenReturn(Types.DATE);
        when(rs.getMetaData()).thenReturn(metaData);

        OngoingStubbing rsStub = when(rs.next()).thenReturn(true);
        for (int i=0; i < 1000; i++) {
            rsStub.thenReturn(true);
        }
        rsStub.thenReturn(false); //Totally 1002 records

        when(rs.getObject(1)).thenReturn("left");
        when(rs.getObject(2)).thenReturn("right");
        when(rs.getObject(3)).thenReturn(null);
        when(rs.getBoolean(4)).thenReturn(true);
        when(rs.getFloat(5)).thenReturn(1.0f);
        when(rs.getInt(6)).thenReturn(1);
        when(rs.getDouble(7)).thenReturn(1d);
        when(rs.getBigDecimal(8)).thenReturn(BigDecimal.ONE);
        when(rs.getDate(9)).thenReturn(new Date(System.currentTimeMillis()));

        csvWriter = new CsvWriter(filePath, headers);
        try  {
            csvWriter.writeRecords(null);
            csvWriter.writeResultSetWithHeader(rs);

            System.out.println("CSV文件创建成功,文件路径:"+filePath);
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            csvWriter.close();
        }

        new File(filePath).delete();
        rs = mock(ResultSet.class);
        metaData = mock(ResultSetMetaData.class);
        when( metaData.getColumnCount() ).thenReturn(9);
        when(metaData.getColumnType(1)).thenReturn(Types.VARCHAR);
        when(metaData.getColumnType(2)).thenReturn(Types.VARCHAR);
        when(metaData.getColumnType(3)).thenReturn(Types.VARCHAR);
        when(metaData.getColumnType(4)).thenReturn(Types.BOOLEAN);
        when(metaData.getColumnType(5)).thenReturn(Types.FLOAT);
        when(metaData.getColumnType(6)).thenReturn(Types.INTEGER);
        when(metaData.getColumnType(7)).thenReturn(Types.DOUBLE);
        when(metaData.getColumnType(8)).thenReturn(Types.DECIMAL);
        when(metaData.getColumnType(9)).thenReturn(Types.DATE);
        when(rs.getMetaData()).thenReturn(metaData);

        rsStub = when(rs.next()).thenReturn(true);
        for (int i=0; i < 1000; i++) {
            rsStub.thenReturn(true);
        }
        rsStub.thenReturn(false); //Totally 1002 records

        when(rs.getObject(1)).thenReturn("left");
        when(rs.getObject(2)).thenReturn("right");
        when(rs.getObject(3)).thenReturn(null);
        when(rs.getBoolean(4)).thenReturn(true);
        when(rs.getFloat(5)).thenReturn(1.0f);
        when(rs.getInt(6)).thenReturn(1);
        when(rs.getDouble(7)).thenReturn(1d);
        when(rs.getBigDecimal(8)).thenReturn(BigDecimal.ONE);
        when(rs.getDate(9)).thenReturn(new Date(System.currentTimeMillis()));

        csvWriter = new CsvWriter(filePath, headers);
        try  {
            csvWriter.writeRecords(null);
            csvWriter.writeResultSetWithOutHeader(rs);

            System.out.println("CSV文件创建成功,文件路径:"+filePath);
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            csvWriter.close();
        }

        new File(filePath).delete();
    }

}
