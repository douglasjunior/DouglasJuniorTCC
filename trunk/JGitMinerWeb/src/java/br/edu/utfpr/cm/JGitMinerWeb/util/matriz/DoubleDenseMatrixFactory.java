/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.util.matriz;

import java.util.*;

/**
 *
 * @author jteodoro
 */
public class DoubleDenseMatrixFactory<T, K> {

    private Collection<T> columns;
    private Collection<K> rows;

    public DoubleDenseMatrixFactory() {
        this.columns = new LinkedList<>();
        this.rows = new LinkedList<>();
    }

    public void addColumn(T column) {
        this.columns.add(column);
    }

    public void addRow(K row) {
        this.rows.add(row);
    }

    public void addColumns(Collection<T> columns) {
        this.columns.addAll(columns);
    }

    public void addRows(Collection<K> rows) {
        this.rows.addAll(rows);
    }

    public DoubleDenseMatrix<T, K> build() {
        return new DoubleDenseMatrix(columns, rows);
    }

    public DoubleDenseMatrix<T, K> join(DoubleDenseMatrix<T, K> left, DoubleDenseMatrix<T, K> right, boolean overrideValues) {

        Set<T> cs = new HashSet<>();
        Set<K> rs = new HashSet<>();
        cs.addAll(left.getColumnKeys());
        cs.addAll(right.getColumnKeys());
        this.addColumns(cs);
        rs.addAll(left.getRowKeys());
        rs.addAll(right.getRowKeys());
        this.addRows(rs);
        DoubleDenseMatrix<T, K> matrix = build();

        for (T t : left.getColumnKeys()) {
            for (K k : left.getRowKeys()) {

                matrix.addValue(t, k, left.getValue(t, k));

            }
        }

        for (T t : right.getColumnKeys()) {

            for (K k : right.getRowKeys()) {

                if (!overrideValues && left.hasColumnValue(t) && left.hasRowValue(k)) {
                    continue;
                } else {
                    matrix.addValue(t, k, right.getValue(t, k));
                }

            }
        }

        return matrix;

    }

    public DoubleDenseMatrix<T, K> build(List<NodeConnection<K, T>> connections, double initWith,
            boolean simetric) {

        Set<K> rws = new HashSet<>();
        Set<T> cls = new HashSet<>();
        Class<T> tClazz = null;
        Class<K> kClazz = null;
        for (NodeConnection<K, T> nc : connections) {
            cls.add(nc.getTo());
            rws.add(nc.getFrom());
            if (tClazz == null && nc.getTo() != null) {
                tClazz = (Class<T>) nc.getTo().getClass();
            }
            if (kClazz == null && nc.getFrom() != null) {
                kClazz = (Class<K>) nc.getFrom().getClass();
            }
        }
        this.addColumns(cls);
        this.addRows(rws);
        DoubleDenseMatrix<T, K> matrix = this.build();
        matrix.initWith(initWith);
        boolean isSimetric = simetric && kClazz.equals(tClazz);
        for (NodeConnection<K, T> nc : connections) {

            matrix.addValue(nc.getTo(), nc.getFrom(), nc.getWeight());
            if (isSimetric) {
                matrix.addValue(tClazz.cast(nc.getFrom()), kClazz.cast(nc.getTo()), nc.getWeight());
            }

        }
        return matrix;
    }
}
