package br.edu.utfpr.cm.JGitMinerWeb.util.matriz;

import Jama.Matrix;
import au.com.bytecode.opencsv.CSVWriter;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class DoubleDenseMatrix<T, K> {

    /**
     * Map<T,index> Map<K,index>
     */
    private final Map<T, Integer> columns;
    private final List<T> columnKeys;
    private final Map<K, Integer> rows;
    private final List<K> rowKeys;
    private final double[][] matrix;
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    public DoubleDenseMatrix(Collection<T> columns, Collection<K> rows) {
        this(columns, rows, new double[rows.size()][columns.size()]);
    }

    public DoubleDenseMatrix(Collection<T> columns, Collection<K> rows,
            double[][] matrix) {
        this.columnKeys = new LinkedList<>(columns);
        this.columns = new LinkedHashMap<>();
        int i = 0;
        for (T t : columns) {
            this.columns.put(t, i++);
        }
        this.rowKeys = new LinkedList<>(rows);
        this.rows = new LinkedHashMap<>();
        i = 0;
        for (K k : rows) {
            this.rows.put(k, i++);
        }
        this.matrix = matrix;
    }

    public void addValue(int column, int row, double value) {
        this.matrix[row][column] = value;
    }

    public void addValue(T column, K row, double value) {
        this.addValue(this.columns.get(column), this.rows.get(row), value);
    }

    public void increment(int column, int row) {
        this.matrix[row][column] = this.matrix[row][column] + 1;
    }

    public void increment(T column, K row) {
        this.increment(this.columns.get(column), this.rows.get(row));
    }

    public Map<T, Integer> getColumnsIndexes() {
        return new LinkedHashMap<>(this.columns);
    }

    public Map<K, Integer> getRowsIndexes() {
        return new LinkedHashMap<>(this.rows);
    }

    public double[][] getMatrix() {

        double[][] nova = new double[this.rows.size()][this.columns.size()];
        for (int i = 0; i < this.rows.size(); i++) {
            System.arraycopy(this.matrix[i], 0, nova[i], 0, this.columnKeys.size());
        }
        return nova;

    }

    public void initWith(double value) {

        for (int i = 0; i < this.columns.size(); i++) {

            for (int j = 0; j < this.rows.size(); j++) {
                this.matrix[j][i] = value;
            }

        }

    }

    public int getColumnIndex(T columnValue) {
        return this.columns.get(columnValue);
    }

    public int getRowIndex(K rowValue) {
        return this.rows.get(rowValue);
    }

    public boolean hasRowValue(K rowValue) {
        return this.rows.containsKey(rowValue);
    }

    public boolean hasColumnValue(T columnValue) {
        return this.columns.containsKey(columnValue);
    }

    public int getRowCount() {
        return this.rows.size();
    }

    public int getColumnCount() {
        return this.columns.size();
    }

    public void toCsv(File destination) throws IOException {
        if (!destination.exists()) {
            destination.createNewFile();
        }

        String[] data = new String[this.columns.size() + 1];

        //write data
        int index = 0;
        data[index] = "row/col";
        for (T t : columnKeys) {
            data[++index] = this.itemToString(t);
        }
        CSVWriter writer = new CSVWriter(new BufferedWriter(new FileWriter(destination)), ';');

        writer.writeNext(data);

        List<K> rowSet = new LinkedList<>(this.rows.keySet());

        //rows
        for (int j = 0; j < this.rows.size(); j++) {
            data[0] = this.itemToString(rowSet.get(j));
            //columns
            for (int i = 0; i < this.columns.size(); i++) {
                data[i + 1] = String.valueOf(this.matrix[j][i]);
            }
            writer.writeNext(data);
        }

        writer.close();
    }

    public DoubleDenseMatrix<K, T> transpose() {

        Matrix m = new Matrix(this.matrix);
        Matrix t = m.transpose();
        return new DoubleDenseMatrix(this.rowKeys, this.columnKeys, t.getArray());

    }

    public DoubleDenseMatrix<T, T> times(DoubleDenseMatrix<K, T> right) {

        Matrix l = new Matrix(this.matrix);
        Matrix r = new Matrix(right.matrix);
        Matrix result = l.times(r);
        return new DoubleDenseMatrix(right.columnKeys, this.rowKeys, result.getArray());

    }

    public double getValue(int column, int row) {
        return this.matrix[row][column];
    }

    public double getValue(T column, K row) {
        return this.getValue(this.columns.get(column), this.rows.get(row));
    }

    public List<K> getRowKeys() {
        return this.rowKeys;
    }

    public List<T> getColumnKeys() {
        return this.columnKeys;
    }

    private String itemToString(Object obj) {
        if (obj instanceof Date) {

            return this.format.format((Date) obj);

        } else {
            return obj.toString();
        }
    }
}
