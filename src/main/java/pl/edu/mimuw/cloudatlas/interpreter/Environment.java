/**
 * Copyright (c) 2014, University of Warsaw
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 * <p>
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package pl.edu.mimuw.cloudatlas.interpreter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueNull;

class Environment {
    private final TableRow row;
    private final Map<String, Integer> columns = new HashMap<String, Integer>();
    private final boolean failOnUndeclaredIdent;

    public Environment(TableRow row, List<String> columns) {
        this.row = row;
        int i = 0;
        for (String c : columns)
            this.columns.put(c, i++);
        this.failOnUndeclaredIdent = false;
    }

    public Environment(TableRow row, List<String> columns, boolean failOnUndeclaredIdent) {
        this.row = row;
        int i = 0;
        for (String c : columns)
            this.columns.put(c, i++);
        this.failOnUndeclaredIdent = failOnUndeclaredIdent;
    }

    public Result getIdent(String ident) {
        try {
            Value v = row.getIth(columns.get(ident));
            if (v.getType().isCollection()) {
                return new ResultColumn(row.getIth(columns.get(ident)));
            } else {
                return new ResultSingle(row.getIth(columns.get(ident)));
            }
        } catch (NullPointerException exception) {
            if (failOnUndeclaredIdent)
                throw new IllegalArgumentException("Undefined ident " + ident + " referenced.");
            return new ResultSingle(ValueNull.getInstance());
        }
    }
}
